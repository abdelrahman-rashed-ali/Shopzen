package shopzen.domain.wishlist.usecase

import kotlinx.coroutines.flow.Flow
import shopzen.domain.wishlist.repository.WishlistRepository
import javax.inject.Inject

class IsProductInWishlistUseCase @Inject constructor(
    private val repository: WishlistRepository
) {
    operator fun invoke(productId: String, userId: String): Flow<Boolean> {
        return repository.isProductInWishlist(productId, userId)
    }
}
