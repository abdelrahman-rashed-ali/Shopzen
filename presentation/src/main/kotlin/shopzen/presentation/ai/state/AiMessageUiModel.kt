package shopzen.presentation.ai.state

data class AiMessageUiModel(
    val id: String,
    val role: AiMessageRoleUi,
    val content: String,
    val products: List<AiProductSuggestionUiModel> = emptyList(),
    val isError: Boolean = false,
)

enum class AiMessageRoleUi {
    USER,
    ASSISTANT,
}

data class AiProductSuggestionUiModel(
    val id: String,
    val title: String,
    val description: String,
    val priceText: String,
    val imageUrl: String?,
    val available: Boolean,
)

