package shopzen.data.customer.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Response ──────────────────────────────────────────────────────────────────

@Serializable
data class CreateCustomerResponseDto(
    @SerialName("customer") val customer: CustomerDto,
)

@Serializable
data class CustomerDto(
    @SerialName("id")         val id: Long,
    @SerialName("email")      val email: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name")  val lastName: String? = null,
)

// ── Request ───────────────────────────────────────────────────────────────────

@Serializable
data class CreateCustomerRequestDto(
    @SerialName("customer") val customer: CustomerRequestBodyDto,
)

@Serializable
data class CustomerRequestBodyDto(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name")  val lastName: String,
    @SerialName("email")      val email: String,
)
