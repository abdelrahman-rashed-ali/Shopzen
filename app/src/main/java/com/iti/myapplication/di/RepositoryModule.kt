package com.iti.myapplication.di

import com.shopzen.data.auth.repository.AuthRepositoryImpl
import com.shopzen.domain.auth.repository.AuthRepository
import dagger.Binds
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
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl,
    ): ProductRepository


    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
