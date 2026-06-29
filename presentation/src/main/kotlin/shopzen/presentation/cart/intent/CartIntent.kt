package shopzen.presentation.cart.intent

sealed class CartIntent {
    // Quantity controls
    data class IncrementQuantity(val itemId: String) : CartIntent()
    data class DecrementQuantity(val itemId: String) : CartIntent()

    // Remove single item (requires confirmation dialog)
    data class RequestRemoveItem(val itemId: String) : CartIntent()
    data object ConfirmRemoveItem : CartIntent()
    data object DismissRemoveItemDialog : CartIntent()

    // Clear entire cart (requires confirmation dialog)
    data object RequestClearCart : CartIntent()
    data object ConfirmClearCart : CartIntent()
    data object DismissClearCartDialog : CartIntent()

    // Coupon bottom sheet
    data object OpenCouponSheet : CartIntent()
    data object DismissCouponSheet : CartIntent()
    data class UpdateCouponCode(val code: String) : CartIntent()
    data object ApplyCoupon : CartIntent()
    data object RemoveCoupon : CartIntent()

    // Navigation
    data object Checkout : CartIntent()
    data class NavigateToProduct(val productId: String) : CartIntent()

    // Error recovery
    data object Retry : CartIntent()
}
