package shopzen.presentation.cart.intent

import shopzen.domain.cart.model.CartItem

sealed class CartIntent {
    data class IncrementQuantity(val itemId: String) : CartIntent()
    data class DecrementQuantity(val itemId: String) : CartIntent()

    data class RequestRemoveItem(val itemId: String) : CartIntent()
    data object ConfirmRemoveItem : CartIntent()
    data object DismissRemoveItemDialog : CartIntent()

    data object RequestClearCart : CartIntent()
    data object ConfirmClearCart : CartIntent()
    data object DismissClearCartDialog : CartIntent()

    data object OpenCouponSheet : CartIntent()
    data object DismissCouponSheet : CartIntent()
    data class UpdateCouponCode(val code: String) : CartIntent()
    data object ApplyCoupon : CartIntent()
    data object RemoveCoupon : CartIntent()

    data object Checkout : CartIntent()
    data class NavigateToProduct(val productId: String) : CartIntent()

    data object Retry : CartIntent()

    data class AddToCart(
        val item: CartItem,
    ) : CartIntent()
}