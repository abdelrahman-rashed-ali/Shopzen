package shopzen.data.catalog.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CollectionDto(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String? = null,
    @SerialName("image") val image: CollectionImageDto? = null
)

@Serializable
data class CollectionImageDto(
    @SerialName("src") val src: String? = null
)

@Serializable
data class CollectionsResponse(
    @SerialName("custom_collections") val customCollections: List<CollectionDto>? = null
)
