package shopzen.domain.cart.usecase

import shopzen.domain.cart.model.CartItem
import javax.inject.Inject

/** Inserts or increments a cart item. Validates stock before insert. Mock until data layer. */
class AddToCartUseCase @Inject constructor() {
    suspend operator fun invoke(item: CartItem): Result<Unit> = Result.success(Unit)
}
