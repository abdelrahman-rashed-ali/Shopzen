package com.iti.myapplication.di

import iti.data.catalog.remote.api.CatalogApiService
import iti.data.catalog.repository.CatalogRepositoryImpl
import iti.domain.catalog.repository.CatalogRepository
import iti.domain.catalog.usecase.GetBrandsUseCase
import iti.domain.catalog.usecase.GetCategoriesUseCase
import iti.domain.catalog.usecase.GetProductsUseCase
import iti.presentation.catalog.viewmodel.HomeViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Manual dependency provider for the catalog feature.
 * Will be replaced by Hilt DI modules once Hilt is configured.
 */
import com.iti.myapplication.BuildConfig

object CatalogModule {

    private val BASE_URL = BuildConfig.SHOPIFY_BASE_URL
    private val ACCESS_TOKEN = BuildConfig.SHOPIFY_ACCESS_TOKEN

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("X-Shopify-Access-Token", ACCESS_TOKEN)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val catalogApiService: CatalogApiService by lazy {
        retrofit.create(CatalogApiService::class.java)
    }

    private val catalogRepository: CatalogRepository by lazy {
        CatalogRepositoryImpl(catalogApiService)
    }

    val getProductsUseCase: GetProductsUseCase by lazy {
        GetProductsUseCase(catalogRepository)
    }

    val getBrandsUseCase: GetBrandsUseCase by lazy {
        GetBrandsUseCase(catalogRepository)
    }

    val getCategoriesUseCase: GetCategoriesUseCase by lazy {
        GetCategoriesUseCase(catalogRepository)
    }

    /**
     * Creates a [HomeViewModel.Factory] with all required use cases.
     */
    fun provideHomeViewModelFactory(): HomeViewModel.Factory {
        return HomeViewModel.Factory(
            getProductsUseCase,
            getBrandsUseCase,
            getCategoriesUseCase
        )
    }
}
