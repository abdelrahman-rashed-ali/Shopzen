package com.iti.myapplication.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import iti.data.product.remote.ProductRemoteDataSource
import iti.data.product.repository.ProductRepositoryImpl
import iti.domain.product.repository.ProductRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepository(
        remoteDataSource: ProductRemoteDataSource,
    ): ProductRepository {
        return ProductRepositoryImpl(remoteDataSource)
    }
}
