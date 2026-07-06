package shopzen.data.checkout.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import shopzen.data.checkout.remote.dto.PaymobPaymentIntentionRequest
import shopzen.data.checkout.remote.dto.PaymobPaymentIntentionResponse
import shopzen.data.remote.qualifier.PaymobClient
import javax.inject.Inject

/**
 * Executes Paymob acceptance operations via Ktor.
 */
class PaymobRemoteDataSource @Inject constructor(
    @PaymobClient private val client: HttpClient,
) {

    /** Creates a Paymob payment intention and returns its client secret response. */
    suspend fun createPaymentIntention(
        request: PaymobPaymentIntentionRequest,
    ): PaymobPaymentIntentionResponse {
        Log.d(TAG, "createPaymentIntention: POST v1/intention amount=${request.amount}")
        val response = client.post("v1/intention/") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val responseBody = response.bodyAsText()
        Log.d(TAG, "createPaymentIntention: HTTP ${response.status.value}")
        if (!response.status.isSuccess()) {
            Log.e(TAG, "createPaymentIntention: non-success body=$responseBody")
            throw IllegalStateException(
                "Paymob intention creation failed (${response.status.value}): $responseBody",
            )
        }
        return json.decodeFromString(responseBody)
    }

    private companion object {
        const val TAG = "PaymobRemoteData"
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
