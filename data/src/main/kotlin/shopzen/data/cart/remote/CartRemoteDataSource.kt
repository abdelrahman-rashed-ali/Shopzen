package shopzen.data.cart.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import shopzen.data.cart.remote.dto.CartDto
import shopzen.data.cart.remote.dto.CartGqlResponse
import shopzen.data.remote.qualifier.GraphQLClient
import javax.inject.Inject

/**
 * Executes Shopify Storefront GraphQL cart operations via Ktor.
 *
 * All methods POST to the pre-configured Storefront endpoint
 * (base URL + auth header are set on [client] by [shopzen.app.di.network.NetworkModule]).
 *
 * Throws [CartRemoteException] on GQL-level errors (userErrors / top-level errors).
 * Network-level exceptions propagate as-is — callers (repository) wrap them in [Result].
 */
class CartRemoteDataSource @Inject constructor(
    @GraphQLClient private val client: HttpClient,
) {

    // ── Public API ─────────────────────────────────────────────────────────────

    /** Creates a new Shopify cart with a single line item. Returns the new [CartDto]. */
    suspend fun createCart(variantId: String, quantity: Int): CartDto {
        val body = gqlBody(
            query = MUTATION_CART_CREATE,
            variables = buildJsonObject {
                putJsonObject("input") {
                    putJsonArray("lines") {
                        addJsonObject {
                            put("merchandiseId", variantId)
                            put("quantity", quantity)
                        }
                    }
                }
            }
        )
        val response: CartGqlResponse = client.post {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
        return response.data?.cartCreate?.cart
            ?: throw CartRemoteException(
                response.data?.cartCreate?.userErrors?.firstOrNull()?.message
                    ?: response.errors?.firstOrNull()?.message
                    ?: "cartCreate returned no cart"
            )
    }

    /** Fetches a cart by its Shopify GID. */
    suspend fun getCart(cartId: String): CartDto {
        val body = gqlBody(
            query = QUERY_GET_CART,
            variables = buildJsonObject { put("id", cartId) }
        )
        val response: CartGqlResponse = client.post {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
        return response.data?.cart
            ?: throw CartRemoteException(
                response.errors?.firstOrNull()?.message ?: "cart query returned no cart"
            )
    }

    /** Adds a variant line to an existing cart. Returns the updated [CartDto]. */
    suspend fun addLines(cartId: String, variantId: String, quantity: Int): CartDto {
        val body = gqlBody(
            query = MUTATION_CART_LINES_ADD,
            variables = buildJsonObject {
                put("cartId", cartId)
                putJsonArray("lines") {
                    addJsonObject {
                        put("merchandiseId", variantId)
                        put("quantity", quantity)
                    }
                }
            }
        )
        val response: CartGqlResponse = client.post {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
        return response.data?.cartLinesAdd?.cart
            ?: throw CartRemoteException(
                response.data?.cartLinesAdd?.userErrors?.firstOrNull()?.message
                    ?: response.errors?.firstOrNull()?.message
                    ?: "cartLinesAdd returned no cart"
            )
    }

    /** Updates the quantity of a specific line in the cart. Returns the updated [CartDto]. */
    suspend fun updateLine(cartId: String, lineId: String, quantity: Int): CartDto {
        val body = gqlBody(
            query = MUTATION_CART_LINES_UPDATE,
            variables = buildJsonObject {
                put("cartId", cartId)
                putJsonArray("lines") {
                    addJsonObject {
                        put("id", lineId)
                        put("quantity", quantity)
                    }
                }
            }
        )
        val response: CartGqlResponse = client.post {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
        return response.data?.cartLinesUpdate?.cart
            ?: throw CartRemoteException(
                response.data?.cartLinesUpdate?.userErrors?.firstOrNull()?.message
                    ?: response.errors?.firstOrNull()?.message
                    ?: "cartLinesUpdate returned no cart"
            )
    }

    /** Removes a line from the cart. Returns the updated [CartDto]. */
    suspend fun removeLine(cartId: String, lineId: String): CartDto {
        return clearLines(cartId, listOf(lineId))
    }

    /** Removes multiple lines from the cart. Returns the updated [CartDto]. */
    suspend fun clearLines(cartId: String, lineIds: List<String>): CartDto {
        val body = gqlBody(
            query = MUTATION_CART_LINES_REMOVE,
            variables = buildJsonObject {
                put("cartId", cartId)
                putJsonArray("lineIds") { lineIds.forEach { add(it) } }
            }
        )
        val response: CartGqlResponse = client.post {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
        return response.data?.cartLinesRemove?.cart
            ?: throw CartRemoteException(
                response.data?.cartLinesRemove?.userErrors?.firstOrNull()?.message
                    ?: response.errors?.firstOrNull()?.message
                    ?: "cartLinesRemove returned no cart"
            )
    }

    /** Updates the discount codes applied to the cart. Returns the updated [CartDto]. */
    suspend fun updateDiscountCodes(cartId: String, discountCodes: List<String>): CartDto {
        val body = gqlBody(
            query = MUTATION_CART_DISCOUNT_CODES_UPDATE,
            variables = buildJsonObject {
                put("cartId", cartId)
                putJsonArray("discountCodes") {
                    discountCodes.forEach { add(it) }
                }
            }
        )
        val response: CartGqlResponse = client.post {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body()
        return response.data?.cartDiscountCodesUpdate?.cart
            ?: throw CartRemoteException(
                response.data?.cartDiscountCodesUpdate?.userErrors?.firstOrNull()?.message
                    ?: response.errors?.firstOrNull()?.message
                    ?: "cartDiscountCodesUpdate returned no cart"
            )
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    @Serializable
    private data class GqlBody(val query: String, val variables: JsonObject)

    private fun gqlBody(query: String, variables: JsonObject = JsonObject(emptyMap())): GqlBody =
        GqlBody(query = query, variables = variables)

    // ── GQL operation strings ──────────────────────────────────────────────────

    private companion object {

        /** Fragment shared across mutations for consistent response shape. */
        private const val CART_FRAGMENT = """
            fragment CartFragment on Cart {
              id
              discountCodes { code applicable }
              lines(first: 50) {
                edges {
                  node {
                    id
                    quantity
                    merchandise {
                      ... on ProductVariant {
                        id
                        title
                        quantityAvailable
                        image { url }
                        product { id title }
                      }
                    }
                    cost { totalAmount { amount currencyCode } }
                  }
                }
              }
              cost {
                subtotalAmount { amount currencyCode }
                totalAmount    { amount currencyCode }
              }
            }
        """

        val MUTATION_CART_CREATE = """
            $CART_FRAGMENT
            mutation cartCreate(${'$'}input: CartInput!) {
              cartCreate(input: ${'$'}input) {
                cart { ...CartFragment }
                userErrors { field message }
              }
            }
        """.trimIndent()

        val QUERY_GET_CART = """
            $CART_FRAGMENT
            query getCart(${'$'}id: ID!) {
              cart(id: ${'$'}id) { ...CartFragment }
            }
        """.trimIndent()

        val MUTATION_CART_LINES_ADD = """
            $CART_FRAGMENT
            mutation cartLinesAdd(${'$'}cartId: ID!, ${'$'}lines: [CartLineInput!]!) {
              cartLinesAdd(cartId: ${'$'}cartId, lines: ${'$'}lines) {
                cart { ...CartFragment }
                userErrors { field message }
              }
            }
        """.trimIndent()

        val MUTATION_CART_LINES_UPDATE = """
            $CART_FRAGMENT
            mutation cartLinesUpdate(${'$'}cartId: ID!, ${'$'}lines: [CartLineUpdateInput!]!) {
              cartLinesUpdate(cartId: ${'$'}cartId, lines: ${'$'}lines) {
                cart { ...CartFragment }
                userErrors { field message }
              }
            }
        """.trimIndent()

        val MUTATION_CART_LINES_REMOVE = """
            $CART_FRAGMENT
            mutation cartLinesRemove(${'$'}cartId: ID!, ${'$'}lineIds: [ID!]!) {
              cartLinesRemove(cartId: ${'$'}cartId, lineIds: ${'$'}lineIds) {
                cart { ...CartFragment }
                userErrors { field message }
              }
            }
        """.trimIndent()

        val MUTATION_CART_DISCOUNT_CODES_UPDATE = """
            $CART_FRAGMENT
            mutation ApplyDiscountCode(${'$'}cartId: ID!, ${'$'}discountCodes: [String!]!) {
              cartDiscountCodesUpdate(cartId: ${'$'}cartId, discountCodes: ${'$'}discountCodes) {
                cart { ...CartFragment }
                userErrors { field message }
              }
            }
        """.trimIndent()
    }
}

/** Thrown when a Storefront GQL operation returns user or schema errors. */
class CartRemoteException(message: String) : Exception(message)
