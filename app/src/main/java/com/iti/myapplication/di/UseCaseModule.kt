package com.iti.myapplication.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import iti.domain.product.repository.ProductRepository
import iti.domain.product.usecase.GetProductByIdUseCase

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetProductByIdUseCase(
        repository: ProductRepository,
    ): GetProductByIdUseCase {
        return GetProductByIdUseCase(repository)
    }
}
