package shopzen.domain.checkout.model

@JvmInline
value class PaymobClientSecret(val value: String)

@JvmInline
value class PaymobPublicKey(val value: String)

@JvmInline
value class PaymobIntentionId(val value: String)

data class PaymobPaymentIntention(
    val clientSecret: PaymobClientSecret,
    val publicKey: PaymobPublicKey,
    val intentionId: PaymobIntentionId,
    val intentionOrderId: Long,
)
