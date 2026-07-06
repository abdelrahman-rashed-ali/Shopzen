package shopzen.domain.customer.usecase

import shopzen.domain.auth.repository.AuthRepository
import shopzen.domain.customer.repository.ShopifyCustomerRepository
import javax.inject.Inject

/**
 * Single source of truth for obtaining the Shopify customer ID of the
 * currently authenticated user.
 *
 * **How it works:**
 * 1. Reads the current Firebase user from [AuthRepository] (no network call).
 * 2. Reads the cached Shopify customer ID from Firestore via
 *    [ShopifyCustomerRepository.getShopifyCustomerId] (one Firestore read).
 * 3. Returns [Result.failure] if:
 *    - No user is signed in.
 *    - The Firestore document has no `shopifyCustomerId` field yet.
 *    - Any I/O error occurs.
 *
 * **Usage contract:**
 * All features that need the Shopify customer ID (Cart, Orders, Addresses,
 * Wishlist, Metafields, Draft Orders) MUST call this use case instead of
 * searching Shopify directly.
 */
class GetCurrentShopifyCustomerIdUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val shopifyCustomerRepository: ShopifyCustomerRepository,
) {
    suspend operator fun invoke(): Result<Long> {
        // ── 1. Resolve Firebase UID ───────────────────────────────────────────
        val user = authRepository.getCurrentUser()
            .getOrElse { cause -> return Result.failure(cause) }
            ?: return Result.failure(IllegalStateException("No authenticated user"))

        // ── 2. Read cached Shopify ID from Firestore ──────────────────────────
        val cachedCustomerId = shopifyCustomerRepository
            .getShopifyCustomerId(uid = user.uid)
            .getOrElse { cause -> return Result.failure(cause) }

        if (cachedCustomerId != null) {
            return Result.success(cachedCustomerId)
        }

        val email = user.email.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Current user has no email"))

        val foundCustomerId = shopifyCustomerRepository
            .findShopifyCustomerIdByEmail(email)
            .getOrElse { cause -> return Result.failure(cause) }
            ?: return Result.failure(
                IllegalStateException(
                    "No Shopify customer ID found for uid=${user.uid} or email=$email."
                )
            )

        shopifyCustomerRepository.saveShopifyCustomerId(user.uid, foundCustomerId)
            .getOrElse { cause -> return Result.failure(cause) }

        return Result.success(foundCustomerId)
    }
}
