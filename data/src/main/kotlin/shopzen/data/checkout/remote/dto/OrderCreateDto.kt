package shopzen.data.checkout.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderCreateGqlResponse(
    val data: OrderCreateDataDto? = null,
    val errors: List<GqlErrorDto>? = null,
)

@Serializable
data class OrderCreateDataDto(
    val orderCreate: OrderCreatePayloadDto? = null,
)

@Serializable
data class OrderCreatePayloadDto(
    val order: OrderDto? = null,
    val userErrors: List<UserErrorDto> = emptyList(),
)

@Serializable
data class OrderDto(
    val id: String,
    val name: String,
)

@Serializable
data class UserErrorDto(
    val field: List<String>? = null,
    val message: String,
)

@Serializable
data class GqlErrorDto(
    val message: String,
)

@Serializable
data class OrderCreateRestResponse(
    val order: OrderDto? = null,
)
