package shopzen.presentation.product.effect

import shopzen.presentation.common.util.UiText

sealed class ProductDetailEffect {
    data object NavigateToLogin : ProductDetailEffect()
    data class ShowSnackbar(val message: UiText) : ProductDetailEffect()
}
