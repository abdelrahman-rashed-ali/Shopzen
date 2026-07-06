package shopzen.data.customer.remote

/**
 * Remote data source for Shopify customer operations.
 *
 * Callers receive plain domain-safe primitives (e.g. [Long]) — no Ktor or
 * serialization types ever leak out of this interface.
 */
interface RemoteShopifyCustomerDataSource {

    /**
     * Creates a new Shopify customer via the Admin REST API.
     *
     * @return The Shopify-assigned numeric customer ID.
     * @throws Exception on network error or non-2xx response.
     */
    suspend fun createCustomer(
        email: String,
        firstName: String,
        lastName: String,
    ): Long
}
