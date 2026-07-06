package shopzen.presentation.profile.intent

sealed class OrderDetailIntent {
    data class LoadOrder(val orderId: String) : OrderDetailIntent()
    data object Retry : OrderDetailIntent()
}
