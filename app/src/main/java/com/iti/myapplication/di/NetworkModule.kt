package com.iti.myapplication.di

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
import iti.data.BuildConfig
import iti.data.product.remote.ProductRemoteDataSource
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = false
                    }
                )
            }

            install(Logging) {
                level = LogLevel.BODY
            }

            defaultRequest {
                url("https://${BuildConfig.SHOPIFY_HOSTNAME}/admin/api/${BuildConfig.SHOPIFY_API_VERSION}/")
                header("X-Shopify-Access-Token", BuildConfig.SHOPIFY_ACCESS_TOKEN)
                header("Content-Type", "application/json")
            }
        }
    }

    @Provides
    @Singleton
    fun provideProductRemoteDataSource(client: HttpClient): ProductRemoteDataSource {
        return ProductRemoteDataSource(client)
    }
}