package shopzen.presentation.cart.state

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CartStateTest {

    @Test
    fun `isEmpty returns true when cart has no items and is not loading`() {
        val state = CartState()

        assertTrue(state.isEmpty)
    }

    @Test
    fun `isEmpty returns false when cart has items`() {
        val state = CartState(items = listOf(sampleItem()))

        assertFalse(state.isEmpty)
    }

    @Test
    fun `isEmpty returns false while loading`() {
        val state = CartState(isLoading = true)

        assertFalse(state.isEmpty)
    }

    private fun sampleItem() = CartItemUi(
        id = "item-1",
        productId = "product-1",
        variantId = "variant-1",
        title = "Blue Hoodie",
        variantTitle = "Size M",
        formattedPrice = "$45.00",
        quantity = 1,
        maxQuantity = 3,
        imageUrl = "",
    )
}
