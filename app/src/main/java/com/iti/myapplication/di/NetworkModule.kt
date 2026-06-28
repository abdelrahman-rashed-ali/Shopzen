package com.iti.myapplication.di

import com.iti.myapplication.BuildConfig
import com.iti.myapplication.remote.config.NetworkConfig
import com.iti.myapplication.remote.rest.KtorRestClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import iti.data.product.remote.ProductRemoteDataSource
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig = NetworkConfig(
        hostname   = BuildConfig.SHOPIFY_HOSTNAME,
        apiVersion = BuildConfig.SHOPIFY_API_VERSION,
        apiKey     = BuildConfig.SHOPIFY_API_KEY,
    )

    @Provides
    @Singleton
    fun provideRestClient(config: NetworkConfig): HttpClient =
        KtorRestClient.build(config)
}