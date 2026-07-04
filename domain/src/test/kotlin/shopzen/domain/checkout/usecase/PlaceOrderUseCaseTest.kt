package shopzen.domain.checkout.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import shopzen.domain.cart.model.CartItem
import shopzen.domain.checkout.model.Checkout
import shopzen.domain.checkout.model.OrderConfirmation
import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.checkout.repository.CheckoutRepository
import shopzen.domain.profile.model.Address

class PlaceOrderUseCaseTest {
    private val repository: CheckoutRepository = mockk()
    private val useCase = PlaceOrderUseCase(repository)

    @Test
    fun `invoke - success - delegates to repository`() = runTest {
        val checkout = checkout()
        val confirmation = OrderConfirmation(orderId = "order-1", orderNumber = "#1001")
        coEvery { repository.placeOrder(checkout) } returns Result.success(confirmation)

        val result = useCase(checkout)

        assertEquals(confirmation, result.getOrNull())
        coVerify(exactly = 1) { repository.placeOrder(checkout) }
    }

    @Test
    fun `invoke - failure - propagates repository failure`() = runTest {
        val checkout = checkout()
        coEvery { repository.placeOrder(checkout) } returns Result.failure(IllegalStateException("Out of stock"))

        val result = useCase(checkout)

        assertTrue(result.isFailure)
        assertEquals("Out of stock", result.exceptionOrNull()?.message)
    }

    private fun checkout(): Checkout =
        Checkout(
            lineItems = listOf(cartItem()),
            shippingAddress = Address(
                id = "address-1",
                recipientName = "Ada Lovelace",
                addressLine1 = "1 Main St",
                city = "Cairo",
                postalCode = "12345",
                country = "Egypt",
                phone = "+201000000000",
            ),
            subtotalPrice = 50.0,
            discountAmount = 0.0,
            totalPrice = 50.0,
            currency = "USD",
            appliedCoupon = null,
            selectedPaymentMethod = PaymentMethod.ONLINE_PAYMENT,
        )

    private fun cartItem(): CartItem =
        CartItem(
            id = "line-1",
            productId = "product-1",
            variantId = "variant-1",
            title = "Shirt",
            variantTitle = "M",
            price = 50.0,
            quantity = 1,
            maxQuantity = 5,
            imageUrl = "",
            userId = "user-1",
        )
}
