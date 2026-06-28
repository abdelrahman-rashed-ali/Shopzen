package iti.data.catalog.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Shopify product JSON shape from REST Admin API.
 */
data class ProductDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String?,
    @SerializedName("vendor") val vendor: String?,
    @SerializedName("product_type") val productType: String?,
    @SerializedName("images") val images: List<ImageDto>?,
    @SerializedName("variants") val variants: List<VariantDto>?,
    @SerializedName("created_at") val createdAt: String?
)

/**
 * Image object within a Shopify product.
 */
data class ImageDto(
    @SerializedName("id") val id: Long,
    @SerializedName("src") val src: String?
)

/**
 * Variant object within a Shopify product.
 */
data class VariantDto(
    @SerializedName("id") val id: Long,
    @SerializedName("price") val price: String?,
    @SerializedName("inventory_quantity") val inventoryQuantity: Int?
)

/**
 * Wrapper for the products list response.
 */
data class ProductsResponse(
    @SerializedName("products") val products: List<ProductDto>?
)
