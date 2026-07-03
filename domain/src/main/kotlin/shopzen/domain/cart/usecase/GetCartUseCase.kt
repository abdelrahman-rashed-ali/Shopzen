package shopzen.domain.cart.usecase

import kotlinx.coroutines.flow.Flow
import shopzen.domain.cart.model.Cart
import shopzen.domain.cart.repository.CartRepository
import javax.inject.Inject

/**
 * Returns a reactive stream of the current user's cart.
 * Emits on every Room mutation; triggers a remote refresh when the cache is stale.
 */
class GetCartUseCase @Inject constructor(
    private val repository: CartRepository,
) {
    operator fun invoke(userId: String, forceRefresh: Boolean = false): Flow<Result<Cart>> = 
        repository.getCart(userId, forceRefresh)
}
