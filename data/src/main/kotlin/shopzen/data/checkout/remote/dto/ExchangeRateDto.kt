package shopzen.data.checkout.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateResponse(
    val result: String,
    @SerialName("base_code")
    val baseCode: String,
    val rates: Map<String, Double> = emptyMap(),
    @SerialName("error-type")
    val errorType: String? = null,
)
