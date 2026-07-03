package shopzen.app.di.customer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import shopzen.data.customer.remote.RemoteShopifyCustomerDataSource
import shopzen.data.customer.remote.RemoteShopifyCustomerDataSourceImpl
import shopzen.data.customer.repository.ShopifyCustomerRepositoryImpl
import shopzen.domain.customer.repository.ShopifyCustomerRepository
import javax.inject.Singleton

/**
 * Hilt module for the Shopify customer feature.
 *
 * Only [Binds] declarations are needed here — all concrete classes use
 * [@Inject constructor][javax.inject.Inject], so Hilt can instantiate them
 * without explicit [@Provides][dagger.Provides] factories.
 *
 * The [HttpClient][io.ktor.client.HttpClient] dependency of
 * [RemoteShopifyCustomerDataSourceImpl] is already provided as a [@Singleton]
 * by [shopzen.app.di.network.NetworkModule] — no duplicate provision needed.
 *
 * The [shopzen.data.profile.remote.RemoteProfileDataSource] dependency of
 * [ShopifyCustomerRepositoryImpl] is already bound in [shopzen.app.di.RepositoryModule].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CustomerModule {

    @Binds
    @Singleton
    abstract fun bindRemoteShopifyCustomerDataSource(
        impl: RemoteShopifyCustomerDataSourceImpl,
    ): RemoteShopifyCustomerDataSource

    @Binds
    @Singleton
    abstract fun bindShopifyCustomerRepository(
        impl: ShopifyCustomerRepositoryImpl,
    ): ShopifyCustomerRepository
}
