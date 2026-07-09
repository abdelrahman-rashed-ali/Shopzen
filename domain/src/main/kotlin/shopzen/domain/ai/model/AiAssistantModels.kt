package shopzen.domain.ai.model

/**
 * Type-safe identifier for chat messages.
 */
@JvmInline
value class AiMessageId(val value: String)

/**
 * User or assistant chat message.
 */
data class AiChatMessage(
    val id: AiMessageId,
    val role: AiMessageRole,
    val content: String,
    val productSuggestions: List<AiProductSuggestion> = emptyList(),
    val pendingAction: PendingAiAction? = null,
)

enum class AiMessageRole {
    USER,
    ASSISTANT,
}

/**
 * Request from the UI state holder into the assistant use case.
 */
data class AiAssistantRequest(
    val message: String,
    val history: List<AiChatMessage>,
    val context: AiConversationContext = AiConversationContext(),
)

/**
 * Assistant response plus updated conversation context.
 */
data class AiAssistantResponse(
    val message: AiChatMessage,
    val context: AiConversationContext,
    val suggestedFollowUps: List<String> = emptyList(),
)

