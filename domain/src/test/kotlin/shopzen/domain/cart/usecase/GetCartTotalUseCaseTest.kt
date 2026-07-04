package shopzen.domain.cart.usecase

import org.junit.Assert.assertEquals
import org.junit.Test
import shopzen.domain.cart.model.CartItem

class GetCartTotalUseCaseTest {
    private val useCase = GetCartTotalUseCase()

    @Test
    fun `invoke - with no discount - returns item subtotal`() {
        val result = useCase(items)

        assertEquals(45.0, result, 0.0)
    }

    @Test
    fun `invoke - with percent-derived discount - subtracts discount`() {
        val subtotal = 45.0
        val discount = subtotal * 20.0 / 100.0

        val result = useCase(items, discount)

        assertEquals(36.0, result, 0.0)
    }

    @Test
    fun `invoke - with fixed discount - subtracts discount`() {
        val result = useCase(items, 15.0)

        assertEquals(30.0, result, 0.0)
    }

    @Test
    fun `invoke - when discount exceeds subtotal - clamps total to zero`() {
        val result = useCase(items, 100.0)

        assertEquals(0.0, result, 0.0)
    }

    @Test
    fun `invoke - with empty cart - returns zero`() {
        val result = useCase(emptyList())

        assertEquals(0.0, result, 0.0)
    }

    private val items = listOf(
        cartItem(id = "1", price = 10.0, quantity = 2),
        cartItem(id = "2", price = 25.0, quantity = 1),
    )

    private fun cartItem(id: String, price: Double, quantity: Int): CartItem =
        CartItem(
            id = id,
            productId = "product-$id",
            variantId = "variant-$id",
            title = "Item $id",
            variantTitle = "Default",
            price = price,
            quantity = quantity,
            maxQuantity = 10,
            imageUrl = "",
            userId = "user-1",
        )
}
