package shopzen.data.checkout.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import shopzen.data.checkout.remote.dto.ExchangeRateResponse
import shopzen.data.remote.qualifier.ExchangeRateClient
import javax.inject.Inject

/** Fetches live exchange rates for checkout payment conversion. */
class ExchangeRateRemoteDataSource @Inject constructor(
    @param:ExchangeRateClient private val client: HttpClient,
) {

    /** Returns a live multiplier from [baseCurrency] to [targetCurrency]. */
    suspend fun getRate(
        baseCurrency: String,
        targetCurrency: String,
    ): Double {
        val base = baseCurrency.trim().uppercase()
        val target = targetCurrency.trim().uppercase()
        if (base == target) return 1.0

        val response = client.get("v6/latest/$base")
        val responseBody = response.bodyAsText()
        Log.d(TAG, "getRate: HTTP ${response.status.value} base=$base target=$target")
        if (!response.status.isSuccess()) {
            throw IllegalStateException(
                "Exchange rate lookup failed (${response.status.value}): $responseBody",
            )
        }

        val payload = json.decodeFromString<ExchangeRateResponse>(responseBody)
        if (payload.result != SUCCESS_RESULT) {
            throw IllegalStateException(
                "Exchange rate lookup failed: ${payload.errorType ?: payload.result}",
            )
        }
        val rate = payload.rates[target]
            ?: throw IllegalStateException("Exchange rate missing for $base to $target")
        require(rate > 0.0) { "Exchange rate must be positive for $base to $target" }
        return rate
    }

    private companion object {
        const val TAG = "ExchangeRateRemote"
        const val SUCCESS_RESULT = "success"
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
