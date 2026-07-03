package shopzen.data.customer.repository

import shopzen.data.customer.remote.RemoteShopifyCustomerDataSource
import shopzen.data.profile.remote.RemoteProfileDataSource
import shopzen.domain.customer.repository.ShopifyCustomerRepository
import javax.inject.Inject

/**
 * Implementation of [ShopifyCustomerRepository].
 *
 * Delegates to two separate data sources:
 * - [RemoteShopifyCustomerDataSource] — Ktor calls to the Shopify Admin API.
 * - [RemoteProfileDataSource] — Firestore reads/writes for the cached ID.
 *
 * These two data sources do NOT depend on each other.  Coordination between
 * Firebase, Shopify, and Firestore is the responsibility of Use Cases (see
 * [shopzen.domain.auth.usecase.RegisterWithEmailUseCase]), not this repository.
 */
class ShopifyCustomerRepositoryImpl @Inject constructor(
    private val remoteShopifyCustomerDataSource: RemoteShopifyCustomerDataSource,
    private val remoteProfileDataSource: RemoteProfileDataSource,
) : ShopifyCustomerRepository {

    override suspend fun createShopifyCustomer(
        email: String,
        firstName: String,
        lastName: String,
    ): Result<Long> = runCatching {
        remoteShopifyCustomerDataSource.createCustomer(
            email     = email,
            firstName = firstName,
            lastName  = lastName,
        )
    }

    override suspend fun saveShopifyCustomerId(uid: String, customerId: Long): Result<Unit> =
        remoteProfileDataSource.saveShopifyCustomerId(uid, customerId)

    override suspend fun getShopifyCustomerId(uid: String): Result<Long?> =
        remoteProfileDataSource.getShopifyCustomerId(uid)
}
