package shopzen.domain.ai.model

/**
 * Product snapshot safe to render inside assistant responses.
 */
data class AiProductSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val price: Double?,
    val currency: String,
    val imageUrl: String?,
    val rating: Double? = null,
    val available: Boolean,
)

