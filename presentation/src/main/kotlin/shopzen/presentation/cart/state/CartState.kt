package shopzen.presentation.cart.state

/**
 * UI model for a single cart item — decoupled from domain CartItem.
 * Carries pre-formatted strings so composables never contain formatting logic.
 */
data class CartItemUi(
    val id: String,
    val productId: String,
    val variantId: String,
    val title: String,
    val variantTitle: String,
    val formattedPrice: String,   // e.g. "$70.00"
    val quantity: Int,
    val maxQuantity: Int,
    val imageUrl: String,
)

/**
 * Full UI state for CartScreen.
 *
 * AGENTS.md §5 convention:
 *   - isLoading, error mandatory
 *   - show*Dialog flags for each destructive action
 *   - pendingRemovalItemId stored alongside its flag
 */
data class CartState(
    val items: List<CartItemUi> = emptyList(),
    val currencySymbol: String = "$",
    val formattedSubtotal: String = "$0.00",
    val formattedDiscount: String? = null,       // null = no coupon applied
    val formattedTotal: String = "$0.00",
    val couponCode: String = "",
    val couponError: String? = null,
    val couponApplied: Boolean = false,
    val appliedCouponLabel: String? = null,      // e.g. "SAVE10 (-10%)"
    val isCouponLoading: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Remove item dialog
    val showRemoveItemDialog: Boolean = false,
    val pendingRemovalItemId: String? = null,
    // Clear cart dialog
    val showClearCartDialog: Boolean = false,
    // Coupon bottom sheet
    val showCouponSheet: Boolean = false,
)

/** Computed: drives EmptyState vs list content. Not in constructor — data class can't have custom getters. */
val CartState.isEmpty: Boolean get() = items.isEmpty() && !isLoading
