package shopzen.data.cart.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Top-level GQL wrapper ──────────────────────────────────────────────────────

@Serializable
data class CartGqlResponse(
    val data: CartGqlData? = null,
    val errors: List<GqlError>? = null,
)

@Serializable
data class CartGqlData(
    val cartCreate: CartPayload? = null,
    val cartLinesAdd: CartPayload? = null,
    val cartLinesUpdate: CartPayload? = null,
    val cartLinesRemove: CartPayload? = null,
    val cart: CartDto? = null,
)

@Serializable
data class CartPayload(
    val cart: CartDto? = null,
    val userErrors: List<UserErrorDto> = emptyList(),
)

@Serializable
data class UserErrorDto(
    val field: List<String>? = null,
    val message: String,
)

@Serializable
data class GqlError(
    val message: String,
    val locations: List<GqlLocation>? = null,
)

@Serializable
data class GqlLocation(
    val line: Int,
    val column: Int,
)

// ── Cart ───────────────────────────────────────────────────────────────────────

@Serializable
data class CartDto(
    val id: String,
    val lines: CartLinesConnectionDto,
    val cost: CartCostDto,
)

@Serializable
data class CartLinesConnectionDto(
    val edges: List<CartLineEdgeDto>,
)

@Serializable
data class CartLineEdgeDto(
    val node: CartLineDto,
)

@Serializable
data class CartLineDto(
    val id: String,
    val quantity: Int,
    val merchandise: MerchandiseDto,
    val cost: CartLineCostDto,
)

// ── Merchandise (product variant) ─────────────────────────────────────────────

@Serializable
data class MerchandiseDto(
    val id: String,
    val title: String,
    val product: ProductRefDto,
    val image: ImageRefDto? = null,
    /** Shopify `quantityAvailable` for stock enforcement. */
    val quantityAvailable: Int = Int.MAX_VALUE,
)

@Serializable
data class ProductRefDto(
    val id: String,
    val title: String,
)

@Serializable
data class ImageRefDto(
    val url: String,
)

// ── Money / Cost ───────────────────────────────────────────────────────────────

@Serializable
data class MoneyDto(
    val amount: String,
    val currencyCode: String,
)

@Serializable
data class CartCostDto(
    val totalAmount: MoneyDto,
    @SerialName("subtotalAmount") val subtotalAmount: MoneyDto,
)

@Serializable
data class CartLineCostDto(
    val totalAmount: MoneyDto,
)
