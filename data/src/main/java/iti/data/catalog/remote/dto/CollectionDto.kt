package iti.data.catalog.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Shopify custom collection JSON shape from REST Admin API.
 */
data class CollectionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("image") val image: CollectionImageDto?
)

/**
 * Image object within a Shopify custom collection.
 */
data class CollectionImageDto(
    @SerializedName("src") val src: String?
)

/**
 * Wrapper for the custom collections list response.
 */
data class CollectionsResponse(
    @SerializedName("custom_collections") val customCollections: List<CollectionDto>?
)
