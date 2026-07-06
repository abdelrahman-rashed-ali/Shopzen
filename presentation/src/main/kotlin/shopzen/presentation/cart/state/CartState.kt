package shopzen.presentation.cart.state

import shopzen.presentation.common.util.UiText

data class CartItemUi(
    val id: String,
    val productId: String,
    val variantId: String,
    val title: String,
    val variantTitle: String,
    val formattedPrice: String,
    val quantity: Int,
    val maxQuantity: Int,
    val imageUrl: String,
)

data class CartState(
    val items: List<CartItemUi> = emptyList(),
    val currencySymbol: String = "$",
    val formattedSubtotal: String = "$0.00",
    val formattedDiscount: String? = null,
    val formattedTotal: String = "$0.00",
    val couponCode: String = "",
    val couponError: UiText? = null,
    val couponApplied: Boolean = false,
    val appliedCouponLabel: String? = null,
    val isCouponLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val showRemoveItemDialog: Boolean = false,
    val pendingRemovalItemId: String? = null,
    val showClearCartDialog: Boolean = false,
    val showCouponSheet: Boolean = false,
)

val CartState.isEmpty: Boolean get() = items.isEmpty() && !isLoading
