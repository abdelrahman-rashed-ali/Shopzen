package shopzen.data.remote.config

data class PaymobConfig(
    val baseUrl: String,
    val publicKey: String,
    val secretKey: String,
    val currency: String,
    val onlineCardIntegrationId: Int,
    val connectTimeoutMs: Long = 15_000L,
    val readTimeoutMs: Long = 15_000L,
    val enableLogging: Boolean = false,
) {
    val enabledPaymentMethodIds: List<Int>
        get() = if (onlineCardIntegrationId > 0) listOf(onlineCardIntegrationId) else emptyList()

    val normalizedCurrency: String
        get() = currency.ifBlank { DEFAULT_CURRENCY }.uppercase()

    val defaultHeaders: Map<String, String>
        get() = mapOf(
            "Accept" to "application/json",
            "Content-Type" to "application/json",
            "Authorization" to "Token $secretKey",
        )

    private companion object {
        const val DEFAULT_CURRENCY = "EGP"
    }
}
