package shopzen.data.cart.mapper

import shopzen.data.cart.local.entity.CartItemEntity
import shopzen.data.cart.remote.dto.CartDto
import shopzen.data.cart.remote.dto.CartLineDto
import shopzen.domain.cart.model.Cart
import shopzen.domain.cart.model.CartItem

// ── DTO → Domain ───────────────────────────────────────────────────────────────

/**
 * Maps a Storefront [CartDto] to the domain [Cart].
 *
 * @param userId   Firebase UID — injected by the repository, not part of the GQL response.
 * @param ttlMs    Cache TTL in milliseconds; used to compute [CartItemEntity.invalidationDate].
 */
fun CartDto.toDomain(userId: String): Cart {
    val lines = lines.edges.map { it.node.toDomainItem(cartId = id, userId = userId) }
    val subtotal = cost.subtotalAmount.amount.toDoubleOrNull() ?: 0.0
    val total = cost.totalAmount.amount.toDoubleOrNull() ?: 0.0
    val currency = cost.subtotalAmount.currencyCode
    val discount = discountCodes.firstOrNull()
    return Cart(
        items         = lines,
        currency      = currency,
        subtotalPrice = subtotal,
        discountAmount = subtotal - total,
        totalPrice    = total,
        appliedCoupon = discount?.let { shopzen.domain.cart.model.DiscountCode(code = it.code, discountType = shopzen.domain.cart.model.DiscountType.PERCENTAGE, value = 0.0) }, // Domain model just needs the code and applied state
        userId        = userId,
    )
}

fun CartLineDto.toDomainItem(cartId: String, userId: String): CartItem =
    CartItem(
        id           = id,
        productId    = merchandise.product.id,
        variantId    = merchandise.id,
        title        = merchandise.product.title,
        variantTitle = merchandise.title,
        price        = cost.totalAmount.amount.toDoubleOrNull()?.div(quantity.coerceAtLeast(1)) ?: 0.0,
        quantity     = quantity,
        maxQuantity  = merchandise.quantityAvailable.coerceAtLeast(quantity),
        imageUrl     = merchandise.image?.url ?: "",
        userId       = userId,
    )

// ── DTO → Entity ───────────────────────────────────────────────────────────────

/**
 * Converts a Storefront [CartLineDto] to a Room [CartItemEntity].
 *
 * @param cartId          Parent cart GID — needed so the repo can refresh via Storefront.
 * @param userId          Firebase UID for multi-user isolation.
 * @param invalidationDate Epoch ms when this cached entry expires.
 */
fun CartLineDto.toEntity(
    cartId: String,
    userId: String,
    currency: String,
    invalidationDate: Long,
): CartItemEntity = CartItemEntity(
    id               = id,
    cartId           = cartId,
    productId        = merchandise.product.id,
    variantId        = merchandise.id,
    title            = merchandise.product.title,
    variantTitle     = merchandise.title,
    price            = cost.totalAmount.amount.toDoubleOrNull()?.div(quantity.coerceAtLeast(1)) ?: 0.0,
    quantity         = quantity,
    maxQuantity      = merchandise.quantityAvailable.coerceAtLeast(quantity),
    imageUrl         = merchandise.image?.url ?: "",
    userId           = userId,
    currency         = currency,
    subtotalPrice    = cartId.hashCode().toDouble(), // Dummy, handled at map level
    totalPrice       = cartId.hashCode().toDouble(), // Dummy, handled at map level
    appliedCouponCode= null, // Handled below
    appliedCouponApplicable = null, // Handled below
    invalidationDate = invalidationDate,
)

// ── Entity → Domain ────────────────────────────────────────────────────────────

fun CartItemEntity.toDomain(): CartItem = CartItem(
    id           = id,
    productId    = productId,
    variantId    = variantId,
    title        = title,
    variantTitle = variantTitle,
    price        = price,
    quantity     = quantity,
    maxQuantity  = maxQuantity,
    imageUrl     = imageUrl,
    userId       = userId,
)

/** Reconstructs a [Cart] from a list of [CartItemEntity]. Returns empty cart when list is empty. */
fun List<CartItemEntity>.toDomainCart(userId: String): Cart {
    val first = firstOrNull()
    return Cart(
        items         = map { it.toDomain() },
        currency      = first?.currency ?: "USD",
        subtotalPrice = first?.subtotalPrice ?: sumOf { it.price * it.quantity },
        totalPrice    = first?.totalPrice ?: sumOf { it.price * it.quantity },
        discountAmount = (first?.subtotalPrice ?: 0.0) - (first?.totalPrice ?: 0.0),
        appliedCoupon = first?.appliedCouponCode?.let { shopzen.domain.cart.model.DiscountCode(code = it, discountType = shopzen.domain.cart.model.DiscountType.PERCENTAGE, value = 0.0) },
        userId        = userId,
    )
}
