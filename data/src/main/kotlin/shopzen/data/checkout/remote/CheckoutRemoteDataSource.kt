package shopzen.data.checkout.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import shopzen.data.checkout.remote.dto.OrderCreateGqlResponse
import shopzen.data.remote.qualifier.RestClient
import javax.inject.Inject

/**
 * Executes Shopify Admin GraphQL checkout operations via Ktor.
 */
class CheckoutRemoteDataSource @Inject constructor(
    @RestClient private val client: HttpClient,
) {

    suspend fun createOrder(
        variables: JsonObject,
        idempotencyKey: IdempotencyKey,
    ): OrderCreateGqlResponse {
        return client.post("graphql.json") {
            contentType(ContentType.Application.Json)
            header("Idempotency-Key", idempotencyKey.value)
            setBody(GqlBody(query = MUTATION_ORDER_CREATE, variables = variables))
        }.body()
    }

    @Serializable
    private data class GqlBody(
        val query: String,
        val variables: JsonObject,
    )

    private companion object {
        val MUTATION_ORDER_CREATE = """
            mutation OrderCreate(${'$'}order: OrderCreateOrderInput!, ${'$'}options: OrderCreateOptionsInput) {
              orderCreate(order: ${'$'}order, options: ${'$'}options) {
                order {
                  id
                  name
                }
                userErrors {
                  field
                  message
                }
              }
            }
        """.trimIndent()
    }
}
