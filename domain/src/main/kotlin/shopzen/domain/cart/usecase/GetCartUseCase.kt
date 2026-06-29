package shopzen.domain.cart.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import shopzen.domain.cart.model.Cart
import shopzen.domain.cart.model.CartItem
import javax.inject.Inject

/**
 * Returns a reactive stream of the current user's cart.
 * Returns static mock data until the data layer is wired.
 */
class GetCartUseCase @Inject constructor() {
    operator fun invoke(userId: String): Flow<Result<Cart>> = flowOf(
        Result.success(
            Cart(
                items = listOf(
                    CartItem(
                        id = "1",
                        productId = "p1",
                        variantId = "v1",
                        title = "Woman Sweater",
                        variantTitle = "Woman Fashion",
                        price = 70.0,
                        quantity = 1,
                        maxQuantity = 10,
                        imageUrl = "",
                        userId = userId,
                    ),
                    CartItem(
                        id = "2",
                        productId = "p2",
                        variantId = "v2",
                        title = "Smart Watch",
                        variantTitle = "Electronics",
                        price = 55.0,
                        quantity = 1,
                        maxQuantity = 5,
                        imageUrl = "",
                        userId = userId,
                    ),
                    CartItem(
                        id = "3",
                        productId = "p3",
                        variantId = "v3",
                        title = "Wireless Headphone",
                        variantTitle = "Electronics",
                        price = 120.0,
                        quantity = 1,
                        maxQuantity = 8,
                        imageUrl = "",
                        userId = userId,
                    ),
                ),
                currency = "$",
                subtotalPrice = 245.0,
                userId = userId,
            )
        )
    )
}
