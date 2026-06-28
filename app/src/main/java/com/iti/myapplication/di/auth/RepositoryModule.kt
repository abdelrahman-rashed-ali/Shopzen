package com.iti.myapplication.di.auth

import com.shopzen.data.auth.remote.RemoteAuthDataSource
import com.shopzen.data.auth.remote.RemoteAuthDataSourceImpl
import com.shopzen.data.auth.repository.AuthRepositoryImpl
import com.shopzen.domain.auth.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRemoteAuthDataSource(
        impl: RemoteAuthDataSourceImpl
    ): RemoteAuthDataSource
}