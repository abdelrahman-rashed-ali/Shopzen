package shopzen.domain.customer.repository

/**
 * Repository interface for Shopify customer operations.
 *
 * - [createShopifyCustomer] calls the Shopify REST API to create the customer record.
 * - [saveShopifyCustomerId] persists the returned ID to Firestore (users/{uid}).
 * - [getShopifyCustomerId] reads the cached ID from Firestore — does NOT call Shopify.
 *
 * Repositories are kept separate from [shopzen.domain.auth.repository.AuthRepository]
 * and [shopzen.domain.profile.repository.ProfileRepository] to respect SOLID /
 * single-responsibility boundaries. Orchestration between them lives exclusively
 * inside Use Cases.
 */
interface ShopifyCustomerRepository {

    /**
     * Creates a new Shopify customer via the Admin REST API.
     * Returns the Shopify-assigned customer ID on success.
     */
    suspend fun createShopifyCustomer(
        email: String,
        firstName: String,
        lastName: String,
    ): Result<Long>

    /**
     * Persists [customerId] under Firestore field `shopifyCustomerId` on
     * the document `users/{uid}`. Merges without overwriting other fields.
     */
    suspend fun saveShopifyCustomerId(uid: String, customerId: Long): Result<Unit>

    /**
     * Reads the previously-saved Shopify customer ID from Firestore.
     * Returns null inside [Result] when the field has not yet been written.
     * Never calls the Shopify API — Firestore is the cache.
     */
    suspend fun getShopifyCustomerId(uid: String): Result<Long?>
}
