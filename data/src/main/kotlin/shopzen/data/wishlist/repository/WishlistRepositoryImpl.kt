package shopzen.data.wishlist.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import shopzen.data.wishlist.local.LocalWishlistDataSource
import shopzen.data.wishlist.mapper.WishlistMapper.toDomain
import shopzen.data.wishlist.mapper.WishlistMapper.toEntity
import shopzen.domain.wishlist.model.WishlistItem
import shopzen.domain.wishlist.repository.WishlistRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepositoryImpl @Inject constructor(
    private val localDataSource: LocalWishlistDataSource
) : WishlistRepository {

    override fun getWishlist(userId: String): Flow<List<WishlistItem>> {
        return localDataSource.getWishlist(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addToWishlist(item: WishlistItem): Result<Unit> {
        return try {
            localDataSource.insertWishlistItem(item.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromWishlist(itemId: String): Result<Unit> {
        return try {
            localDataSource.deleteWishlistItem(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isProductInWishlist(productId: String, userId: String): Flow<Boolean> {
        return localDataSource.isProductInWishlist(productId, userId)
    }
}
