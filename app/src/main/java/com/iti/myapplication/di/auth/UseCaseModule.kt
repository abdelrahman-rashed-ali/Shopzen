package com.iti.myapplication.di.auth

import com.shopzen.domain.auth.repository.AuthRepository
import com.shopzen.domain.auth.usecase.RegisterWithEmailUseCase
import com.shopzen.domain.auth.usecase.SendVerificationEmailUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideRegisterWithEmailUseCase(
        authRepository: AuthRepository
    ): RegisterWithEmailUseCase = RegisterWithEmailUseCase(authRepository)

    @Provides
    @Singleton
    fun provideSendVerificationEmailUseCase(
        authRepository: AuthRepository
    ): SendVerificationEmailUseCase = SendVerificationEmailUseCase(authRepository)
}