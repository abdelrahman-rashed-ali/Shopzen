package iti.data.product.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level DTO matching the Shopify REST Admin API response:
 * GET /admin/api/{version}/products/{id}.json
 *
 * Wraps a single [ProductDto].
 */
@Serializable
data class ProductResponseDto(
    val product: ProductDto,
)

@Serializable
data class ProductDto(
    val id: Long,
    val title: String = "",
    @SerialName("body_html") val bodyHtml: String? = null,
    val vendor: String = "",
    @SerialName("product_type") val productType: String = "",
    val tags: String = "",
    val variants: List<VariantDto> = emptyList(),
    val options: List<OptionDto> = emptyList(),
    val images: List<ImageDto> = emptyList(),
)

@Serializable
data class VariantDto(
    val id: Long,
    val title: String = "",
    val price: String = "0.00",
    @SerialName("compare_at_price") val compareAtPrice: String? = null,
    @SerialName("inventory_quantity") val inventoryQuantity: Int = 0,
    val option1: String? = null,
    val option2: String? = null,
    val option3: String? = null,
)

@Serializable
data class OptionDto(
    val id: Long,
    val name: String = "",
    val values: List<String> = emptyList(),
)

@Serializable
data class ImageDto(
    val id: Long,
    val src: String = "",
    val alt: String? = null,
)
