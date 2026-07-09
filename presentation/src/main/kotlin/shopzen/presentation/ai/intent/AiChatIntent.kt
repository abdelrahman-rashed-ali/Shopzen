package shopzen.presentation.ai.intent

sealed interface AiChatIntent {
    data class SendMessage(val text: String) : AiChatIntent
    data class ConfirmAction(val actionId: String) : AiChatIntent
    data class CancelAction(val actionId: String) : AiChatIntent
    data object RetryLastMessage : AiChatIntent
    data object ClearChat : AiChatIntent
    data class OpenProduct(val productId: String) : AiChatIntent
}

