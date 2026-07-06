package shopzen.domain.checkout.usecase

import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.OrderConfirmation
import shopzen.domain.checkout.repository.CheckoutRepository
import javax.inject.Inject

/** Places a checkout order through the checkout repository. */
class PlaceOrderUseCase @Inject constructor(
    private val repository: CheckoutRepository,
) {
    /** Delegates order placement and returns the repository result unchanged. */
    suspend operator fun invoke(checkout: Checkout): Result<OrderConfirmation> =
        repository.placeOrder(checkout)
}
