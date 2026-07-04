package shopzen.app.di.network

import shopzen.data.remote.config.NetworkConfig
import shopzen.data.remote.graphql.KtorGraphQLClient
import shopzen.data.remote.qualifier.GraphQLClient
import shopzen.data.remote.qualifier.RestClient
import shopzen.data.remote.rest.KtorRestClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import shopzen.app.BuildConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig = NetworkConfig(
        hostname        = BuildConfig.SHOPIFY_HOSTNAME,
        apiVersion      = BuildConfig.SHOPIFY_API_VERSION,
        apiKey          = BuildConfig.SHOPIFY_API_KEY,
        apiPassword     = BuildConfig.SHOPIFY_PASSWORD,
        storefrontToken = BuildConfig.SHOPIFY_STOREFRONT_TOKEN,
    )

    @Provides
    @Singleton
    @RestClient
    fun provideRestClient(config: NetworkConfig): HttpClient =
        KtorRestClient.build(config)

    @Provides
    @Singleton
    @GraphQLClient
    fun provideGraphQLClient(config: NetworkConfig): HttpClient =
        KtorGraphQLClient.build(config)
}
