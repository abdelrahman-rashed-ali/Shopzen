package shopzen.presentation.profile.state

import shopzen.domain.profile.model.Order
import shopzen.presentation.common.util.UiText

data class OrderHistoryState(
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val orders: List<Order> = emptyList(),
)
