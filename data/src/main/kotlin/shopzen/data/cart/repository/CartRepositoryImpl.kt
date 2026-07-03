package shopzen.data.cart.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import shopzen.data.cart.local.CartLocalDataSource
import shopzen.data.cart.mapper.toEntity
import shopzen.data.cart.mapper.toDomain
import shopzen.data.cart.mapper.toDomainCart
import shopzen.data.cart.remote.CartRemoteDataSource
import shopzen.data.cart.remote.CartSyncAdapter
import shopzen.domain.cart.model.Cart
import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.model.CouponValidationResult
import shopzen.domain.cart.repository.CartRepository
import javax.inject.Inject

/**
 * Concrete [CartRepository] backed by:
 * - **Remote**: Shopify Storefront GraphQL via [CartRemoteDataSource]
 * - **Local**:  Room cache via [CartLocalDataSource]
 *
 * ## Cache invalidation strategy
 * Each [CartItemEntity][shopzen.data.cart.local.entity.CartItemEntity] stores an
 * [invalidationDate][shopzen.data.cart.local.entity.CartItemEntity.invalidationDate]
 * (epoch ms). On [getCart]:
 *  1. Emit Room's current [Flow] immediately (offline-first).
 *  2. Before first emission, check if ANY item for this user is expired
 *     (`invalidationDate < now`). If yes → fetch from Storefront, upsert Room with
 *     a fresh [invalidationDate]. Room's [Flow] re-emits automatically.
 *
 * Mutations (add / update / remove) always:
 *  1. Call the remote Storefront operation.
 *  2. On success, upsert the full refreshed cart into Room with a new [invalidationDate].
 *  3. Room's [Flow] re-emits — UI stays reactive without manual refresh.
 */
class CartRepositoryImpl @Inject constructor(
    private val remote: CartRemoteDataSource,
    private val local: CartLocalDataSource,
    private val syncAdapter: CartSyncAdapter,
) : CartRepository {

    companion object {
        /** Cache time-to-live: 1 week */
        const val CACHE_TTL_MS = 7 * 24 * 60 * 60 * 1_000L
    }

    // ── Observe ────────────────────────────────────────────────────────────────

    override fun getCart(userId: String, forceRefresh: Boolean): Flow<Result<Cart>> =
        local.observeCart(userId)
            .map { entities -> Result.success(entities.toDomainCart(userId)) }
            .onStart {
                // Trigger remote refresh if cache is stale (or empty) or forced.
                refreshIfStale(userId, forceRefresh)
            }

    // ── Mutations ──────────────────────────────────────────────────────────────

    override suspend fun addToCart(item: CartItem): Result<Unit> = runCatching {
        val cartId = requireCartIdOrNull(item.userId)

        val refreshedCart = if (cartId == null) {
            // No cart yet (local or remote) — create one.
            val newCart = remote.createCart(variantId = item.variantId, quantity = item.quantity)
            syncAdapter.saveCartId(item.userId, newCart.id)
            newCart
        } else {
            // Cart exists — add a new line (Storefront deduplicates same variant automatically).
            remote.addLines(
                cartId    = cartId,
                variantId = item.variantId,
                quantity  = item.quantity,
            )
        }

        upsertCartToLocal(refreshedCart, userId = item.userId)
    }

    override suspend fun removeFromCart(itemId: String, userId: String): Result<Unit> = runCatching {
        val cartId = requireCartId(userId)
        val refreshedCart = remote.removeLine(cartId = cartId, lineId = itemId)
        upsertCartToLocal(refreshedCart, userId)
    }

    override suspend fun updateItemQuantity(
        itemId: String,
        quantity: Int,
        userId: String,
    ): Result<Unit> = runCatching {
        val cartId = requireCartId(userId)
        val refreshedCart = remote.updateLine(
            cartId   = cartId,
            lineId   = itemId,
            quantity = quantity,
        )
        upsertCartToLocal(refreshedCart, userId)
    }

    override suspend fun clearCart(userId: String): Result<Unit> = runCatching {
        // Local-only; no Storefront mutation for "clear all".
        local.clearCart(userId)
    }

    // ── Coupon / Currency (delegated to other features per AGENTS.md) ──────────

    override suspend fun validateCoupon(code: String): Result<CouponValidationResult> =
        Result.failure(NotImplementedError("Coupon validation belongs to the checkout feature slice."))

    override suspend fun getCurrencySymbol(): String = "USD"

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Checks whether any cached item for [userId] is past its [invalidationDate], or if [forceRefresh] is true.
     * If stale (or no cached items exist), fetches the full cart from Storefront and upserts Room.
     */
    private suspend fun refreshIfStale(userId: String, forceRefresh: Boolean) {
        val cached = local.getCart(userId)
        val now = System.currentTimeMillis()

        val isStale = forceRefresh || cached.isEmpty() || cached.any { it.invalidationDate < now }
        if (!isStale) return

        val cartId = requireCartIdOrNull(userId) ?: return  // No cartId → nothing to refresh.
        runCatching {
            val remoteCart = remote.getCart(cartId)
            upsertCartToLocal(remoteCart, userId)
        }
        // Swallow remote errors during background refresh; Room flow already emitted cached data.
    }

    /**
     * Maps the Storefront [CartDto][shopzen.data.cart.remote.dto.CartDto] lines to entities
     * and upserts them into Room with a fresh [invalidationDate].
     *
     * Also removes any local lines that are no longer present in the remote cart
     * (e.g. removed via another device session).
     */
    private suspend fun upsertCartToLocal(
        cart: shopzen.data.cart.remote.dto.CartDto,
        userId: String,
    ) {
        val newInvalidationDate = System.currentTimeMillis() + CACHE_TTL_MS
        val currency = cart.cost.subtotalAmount.currencyCode

        val entities = cart.lines.edges.map { edge ->
            edge.node.toEntity(
                cartId           = cart.id,
                userId           = userId,
                currency         = currency,
                invalidationDate = newInvalidationDate,
            )
        }

        // Reconcile: clear old rows and insert fresh snapshot.
        local.clearCart(userId)
        local.upsertAll(entities)
    }

    /**
     * Retrieves the Shopify cart GID for [userId] from the local cache or Firestore.
     * Throws [IllegalStateException] if no cart has been created yet.
     */
    private suspend fun requireCartId(userId: String): String =
        requireCartIdOrNull(userId)
            ?: error("No Shopify cartId found for user $userId — create a cart first.")

    /**
     * Retrieves the Shopify cart GID for [userId].
     * Checks Room first. If missing, checks Firestore. Returns null if missing in both.
     */
    private suspend fun requireCartIdOrNull(userId: String): String? {
        val localCartId = local.getCart(userId).firstOrNull()?.cartId
        if (localCartId != null) return localCartId

        // If local is empty (e.g. app data cleared or logged in on new device), check Firestore.
        val remoteCartId = syncAdapter.getCartId(userId)
        return remoteCartId
    }
}
