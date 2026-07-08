package shopzen.presentation.common.mapper

import shopzen.domain.cart.model.CartException
import shopzen.presentation.R
import shopzen.presentation.common.util.UiText

/**
 * Maps typed domain/data failures into user-friendly localized [UiText] messages.
 */
fun Throwable.toUiText(fallbackMessageId: Int = R.string.cart_error_mutation_failed): UiText {
    return when (this) {
        is CartException.OutOfStock -> UiText.StringResource(R.string.product_detail_out_of_stock)
        is CartException.MutationFailed -> UiText.StringResource(R.string.cart_error_mutation_failed)
        else -> message?.let(UiText::DynamicString) ?: UiText.StringResource(fallbackMessageId)
    }
}
