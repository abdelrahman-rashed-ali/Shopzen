package shopzen.data.checkout.mapper

import shopzen.data.checkout.remote.dto.PaymobBillingDataRequest
import shopzen.data.checkout.remote.dto.PaymobPaymentIntentionRequest
import shopzen.data.checkout.remote.dto.PaymobPaymentIntentionResponse
import shopzen.data.checkout.remote.dto.PaymobPaymentItemRequest
import shopzen.data.remote.config.PaymobConfig
import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.PaymobClientSecret
import shopzen.domain.checkout.model.PaymobIntentionId
import shopzen.domain.checkout.model.PaymobPaymentIntention
import shopzen.domain.checkout.model.PaymobPublicKey
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class PaymobMapper @Inject constructor() {

    /** Maps a checkout into the Paymob intention request expected by the mobile SDK flow. */
    fun toPaymentIntentionRequest(
        checkout: Checkout,
        config: PaymobConfig,
        specialReference: String,
        exchangeRate: Double,
    ): PaymobPaymentIntentionRequest {
        val paymentAmount = checkout.totalPriceInPaymentCurrency(
            paymentCurrency = config.normalizedCurrency,
            exchangeRate = exchangeRate,
        )
        val amountInCents = paymentAmount.toCents()
        return PaymobPaymentIntentionRequest(
            amount = amountInCents,
            currency = config.normalizedCurrency,
            paymentMethods = config.enabledPaymentMethodIds,
            items = listOf(
                PaymobPaymentItemRequest(
                    name = SHOPZEN_ORDER_ITEM_NAME,
                    amount = amountInCents,
                    description = "${checkout.lineItems.sumOf { it.quantity }} checkout item(s)",
                    quantity = 1,
                )
            ),
            billingData = checkout.toBillingDataRequest(),
            specialReference = specialReference,
        )
    }

    /** Maps Paymob's response into domain launch data for the SDK. */
    fun toPaymentIntention(
        response: PaymobPaymentIntentionResponse,
        config: PaymobConfig,
    ): PaymobPaymentIntention =
        PaymobPaymentIntention(
            clientSecret = PaymobClientSecret(response.clientSecret),
            publicKey = PaymobPublicKey(config.publicKey),
            intentionId = PaymobIntentionId(response.id),
            intentionOrderId = response.intentionOrderId,
        )

    private fun Checkout.toBillingDataRequest(): PaymobBillingDataRequest {
        val nameParts = shippingAddress.recipientName.ifBlank { "Shopzen Customer" }
            .trim()
            .split(Regex("\\s+"), limit = 2)
        return PaymobBillingDataRequest(
            firstName = nameParts.firstOrNull().orEmpty().ifBlank { "Shopzen" },
            lastName = nameParts.getOrNull(1).orEmpty().ifBlank { "Customer" },
            phoneNumber = shippingAddress.phone.orEmpty(),
            email = customerEmail,
            street = listOfNotNull(shippingAddress.addressLine1, shippingAddress.addressLine2)
                .filter { it.isNotBlank() }
                .joinToString(", ")
                .ifBlank { null },
            city = shippingAddress.city.ifBlank { null },
            country = shippingAddress.country.ifBlank { null },
            state = shippingAddress.stateOrProvince,
            postalCode = shippingAddress.postalCode.ifBlank { null },
        )
    }

    private fun Checkout.totalPriceInPaymentCurrency(
        paymentCurrency: String,
        exchangeRate: Double,
    ): Double =
        when {
            currency.trim().uppercase() == paymentCurrency -> totalPrice
            exchangeRate > 0.0 -> totalPrice * exchangeRate
            else -> error("Missing exchange rate for ${currency.trim().uppercase()} to $paymentCurrency")
        }

    private fun Double.toCents(): Int =
        BigDecimal.valueOf(this)
            .multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .toInt()

    private companion object {
        const val SHOPZEN_ORDER_ITEM_NAME = "Shopzen order"
    }
}
