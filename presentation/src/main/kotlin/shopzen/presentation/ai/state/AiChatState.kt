package shopzen.presentation.ai.state

import shopzen.domain.ai.model.PendingAiAction

data class AiChatState(
    val messages: List<AiMessageUiModel> = emptyList(),
    val pendingActions: List<PendingAiAction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val suggestions: List<String> = listOf(
        "Show me shoes under 2000 EGP",
        "What categories do you have?",
        "What is in my cart?",
    ),
    val currentInput: String = "",
    val lastUserMessage: String? = null,
)

