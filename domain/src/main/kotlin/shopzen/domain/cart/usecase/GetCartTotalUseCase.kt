package shopzen.domain.cart.usecase

import shopzen.domain.cart.model.CartItem
import javax.inject.Inject

/** Pure computation: sum(item.price * item.quantity). No network call. */
class GetCartTotalUseCase @Inject constructor() {
    operator fun invoke(items: List<CartItem>, discountAmount: Double = 0.0): Double =
        (items.sumOf { it.price * it.quantity } - discountAmount).coerceAtLeast(0.0)
}
