package shopzen.domain.wishlist.usecase

import kotlinx.coroutines.flow.Flow
import shopzen.domain.wishlist.model.WishlistItem
import shopzen.domain.wishlist.repository.WishlistRepository
import javax.inject.Inject

class GetWishlistUseCase @Inject constructor(
    private val repository: WishlistRepository
) {
    operator fun invoke(userId: String): Flow<List<WishlistItem>> {
        return repository.getWishlist(userId)
    }
}
