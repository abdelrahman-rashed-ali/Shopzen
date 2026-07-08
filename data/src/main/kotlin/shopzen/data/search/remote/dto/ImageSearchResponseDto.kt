package shopzen.data.search.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ImageSearchResponseDto(
    @SerialName("data")
    val data: List<ImageSearchProductDto> = emptyList()
)

@Serializable
data class ImageSearchProductDto(
    @SerialName("id")
    val id: String,
    @SerialName("title")
    val title: String,
    @SerialName("handle")
    val handle: String? = null,
    @SerialName("vendor")
    val vendor: String,
    @SerialName("productType")
    val productType: String,
    @SerialName("featuredImage")
    val featuredImage: String? = null,
    @SerialName("price")
    val price: String,
    @SerialName("compareAtPrice")
    val compareAtPrice: String? = null,
    @SerialName("inventory")
    val inventory: Int? = null,
    @SerialName("score")
    val score: Double? = null
)
