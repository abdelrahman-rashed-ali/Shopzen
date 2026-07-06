package shopzen.data.profile.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import shopzen.data.profile.remote.dto.OrderDetailResponseDto
import shopzen.data.profile.remote.dto.OrderHistoryResponseDto
import shopzen.data.remote.qualifier.RestClient
import javax.inject.Inject

/**
 * Executes Shopify Admin REST order requests.
 */
class RemoteOrderDataSource @Inject constructor(
    @param:RestClient private val client: HttpClient,
) {
    /**
     * Fetches the latest orders for a Shopify customer.
     */
    suspend fun getOrderHistory(customerId: Long): OrderHistoryResponseDto {
        val response = client.get("customers/$customerId/orders.json") {
            url {
                parameters.append("status", "any")
                parameters.append("limit", "50")
            }
        }
        val responseBody = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException(
                "Shopify order history failed (${response.status.value}): $responseBody",
            )
        }
        return json.decodeFromString<OrderHistoryResponseDto>(responseBody)
    }

    /**
     * Fetches one Shopify order by its Admin REST ID.
     */
    suspend fun getOrderDetail(orderId: String): OrderDetailResponseDto {
        val response = client.get("orders/$orderId.json")
        val responseBody = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw IllegalStateException(
                "Shopify order detail failed (${response.status.value}): $responseBody",
            )
        }
        return json.decodeFromString<OrderDetailResponseDto>(responseBody)
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}
