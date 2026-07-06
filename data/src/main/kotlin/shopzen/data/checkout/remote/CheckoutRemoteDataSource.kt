package shopzen.data.checkout.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import shopzen.data.checkout.remote.dto.OrderCreateRestResponse
import shopzen.data.remote.qualifier.RestClient
import javax.inject.Inject

/**
 * Executes Shopify Admin REST checkout operations via Ktor.
 */
class CheckoutRemoteDataSource @Inject constructor(
    @RestClient private val client: HttpClient,
) {

    suspend fun createOrder(
        body: JsonObject,
        idempotencyKey: IdempotencyKey,
    ): OrderCreateRestResponse {
        Log.d(TAG, "createOrder: POST orders.json idempotencyKey=${idempotencyKey.value}")
        return try {
            val response = client.post("orders.json") {
                contentType(ContentType.Application.Json)
                header("Idempotency-Key", idempotencyKey.value)
                setBody(body)
            }
            val responseBody = response.bodyAsText()
            Log.d(
                TAG,
                "createOrder: HTTP ${response.status.value} url=${response.call.request.url}",
            )
            if (!response.status.isSuccess()) {
                Log.e(
                    TAG,
                    "createOrder: non-success status=${response.status.value} body=$responseBody",
                )
                throw IllegalStateException(
                    "Shopify order create failed (${response.status.value}): $responseBody",
                )
            }
            json.decodeFromString<OrderCreateRestResponse>(responseBody)
        } catch (e: Exception) {
            Log.e(TAG, "createOrder: request failed before response", e)
            throw e
        }
    }

    private companion object {
        const val TAG = "CheckoutRemoteData"
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
