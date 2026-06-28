package com.iti.myapplication.di.network

import com.iti.myapplication.remote.config.NetworkConfig
import com.iti.myapplication.remote.rest.KtorRestClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import iti.app.BuildConfig
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