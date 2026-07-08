package shopzen.presentation.wishlist.state

import shopzen.domain.wishlist.model.WishlistItem

data class WishlistState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<WishlistItem> = emptyList(),
    val showRemoveItemDialog: Boolean = false,
    val pendingRemovalItemId: String? = null,
    val showAddConfirmationDialog: Boolean = false,
    val pendingAddProduct: shopzen.domain.catalog.model.Product? = null,
    val showLoginRequiredDialog: Boolean = false,
    val currency: shopzen.domain.profile.model.AppCurrency = shopzen.domain.profile.model.AppCurrency.USD
)
