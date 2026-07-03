package shopzen.domain.wishlist.usecase

import shopzen.domain.wishlist.repository.WishlistRepository
import javax.inject.Inject

class RemoveFromWishlistUseCase @Inject constructor(
    private val repository: WishlistRepository
) {
    suspend operator fun invoke(itemId: String): Result<Unit> {
        return repository.removeFromWishlist(itemId)
    }
}
