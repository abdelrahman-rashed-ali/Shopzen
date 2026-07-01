package shopzen.app.di

import shopzen.data.catalog.repository.CatalogRepositoryImpl
import shopzen.domain.catalog.repository.CatalogRepository
import shopzen.data.auth.repository.AuthRepositoryImpl
import shopzen.domain.auth.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import shopzen.data.product.repository.ProductRepositoryImpl
import shopzen.domain.product.repository.ProductRepository
import shopzen.data.onboarding.repository.OnboardingRepositoryImpl
import shopzen.domain.onboarding.repository.OnboardingRepository
import shopzen.data.wishlist.repository.WishlistRepositoryImpl
import shopzen.domain.wishlist.repository.WishlistRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(
        impl: CatalogRepositoryImpl
    ): CatalogRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl,
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindWishlistRepository(
        impl: WishlistRepositoryImpl
    ): WishlistRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        impl: OnboardingRepositoryImpl
    ): OnboardingRepository
}
