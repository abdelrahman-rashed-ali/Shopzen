package shopzen.domain.ads.model

data class Ad(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val ctaText: String? = null,
    val targetUrl: String? = null
)
