package shopzen.data.catalog.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String? = null,
    @SerialName("vendor") val vendor: String? = null,
    @SerialName("product_type") val productType: String? = null,
    @SerialName("images") val images: List<ImageDto>? = null,
    @SerialName("variants") val variants: List<VariantDto>? = null
)

@Serializable
data class ImageDto(
    @SerialName("id") val id: Long,
    @SerialName("src") val src: String? = null
)

@Serializable
data class VariantDto(
    @SerialName("id") val id: Long,
    @SerialName("price") val price: String? = null,
    @SerialName("inventory_quantity") val inventoryQuantity: Int? = null
)

@Serializable
data class ProductsResponse(
    @SerialName("products") val products: List<ProductDto>? = null
)
