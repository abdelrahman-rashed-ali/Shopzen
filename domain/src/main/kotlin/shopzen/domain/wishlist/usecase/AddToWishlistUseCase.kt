package shopzen.domain.wishlist.usecase

import shopzen.domain.wishlist.model.WishlistItem
import shopzen.domain.wishlist.repository.WishlistRepository
import javax.inject.Inject

class AddToWishlistUseCase @Inject constructor(
    private val repository: WishlistRepository
) {
    suspend operator fun invoke(item: WishlistItem): Result<Unit> {
        return repository.addToWishlist(item)
    }
}
