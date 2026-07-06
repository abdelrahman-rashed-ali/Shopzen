package shopzen.presentation.profile.state

import shopzen.domain.profile.model.Order
import shopzen.presentation.common.util.UiText

data class OrderDetailState(
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val order: Order? = null,
)
