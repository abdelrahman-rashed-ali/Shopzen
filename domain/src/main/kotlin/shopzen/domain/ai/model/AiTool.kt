package shopzen.domain.ai.model

/**
 * Read-only or confirmed action tool contract for assistant orchestration.
 */
interface AiTool {
    val name: String
    val description: String
    val requiresConfirmation: Boolean

    /**
     * Executes this tool with validated string arguments.
     */
    suspend fun execute(arguments: Map<String, String>): AiToolResult
}

/**
 * Finds tools by name and exposes the currently registered set.
 */
interface AiToolRegistry {
    val tools: List<AiTool>

    /**
     * Returns a registered tool by exact name, or null when absent.
     */
    fun find(name: String): AiTool? = tools.firstOrNull { it.name == name }
}

/**
 * Structured tool execution result safe to pass back to UI/data mapping.
 */
data class AiToolResult(
    val success: Boolean,
    val content: String,
    val structuredData: Map<String, String> = emptyMap(),
)

/**
 * Tool request captured for logging, validation, or future gateway execution.
 */
data class AiToolRequest(
    val name: String,
    val arguments: Map<String, String>,
)

