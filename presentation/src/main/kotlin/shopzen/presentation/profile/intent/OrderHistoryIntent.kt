package shopzen.presentation.profile.intent

sealed class OrderHistoryIntent {
    data object LoadOrders : OrderHistoryIntent()
    data object Retry : OrderHistoryIntent()
}
