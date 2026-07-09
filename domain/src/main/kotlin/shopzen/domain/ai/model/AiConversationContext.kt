package shopzen.domain.ai.model

/**
 * Carries small, app-owned context between assistant turns.
 */
data class AiConversationContext(
    val recentProductIds: List<String> = emptyList(),
)

