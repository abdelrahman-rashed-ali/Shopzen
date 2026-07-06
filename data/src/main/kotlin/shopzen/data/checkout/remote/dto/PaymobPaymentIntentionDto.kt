package shopzen.data.checkout.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymobPaymentIntentionRequest(
    val amount: Int,
    val currency: String,
    @SerialName("payment_methods")
    val paymentMethods: List<Int>,
    val items: List<PaymobPaymentItemRequest>,
    @SerialName("billing_data")
    val billingData: PaymobBillingDataRequest,
    @SerialName("special_reference")
    val specialReference: String? = null,
)

@Serializable
data class PaymobPaymentItemRequest(
    val name: String,
    val amount: Int,
    val description: String,
    val quantity: Int,
)

@Serializable
data class PaymobBillingDataRequest(
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("phone_number")
    val phoneNumber: String,
    val email: String,
    val street: String? = null,
    val city: String? = null,
    val country: String? = null,
    val state: String? = null,
    @SerialName("postal_code")
    val postalCode: String? = null,
    val apartment: String? = null,
    val floor: String? = null,
    val building: String? = null,
)

@Serializable
data class PaymobPaymentIntentionResponse(
    @SerialName("client_secret")
    val clientSecret: String,
    @SerialName("intention_order_id")
    val intentionOrderId: Long,
    val id: String,
    val status: String? = null,
)
