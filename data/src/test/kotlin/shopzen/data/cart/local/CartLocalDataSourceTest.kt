package shopzen.data.cart.local

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import shopzen.data.cart.local.dao.CartDao
import shopzen.data.cart.local.entity.CartItemEntity

class CartLocalDataSourceTest {

    private lateinit var dao: CartDao
    private lateinit var dataSource: CartLocalDataSource

    @Before
    fun setUp() {
        dao = mockk()
        dataSource = CartLocalDataSource(dao)
    }

    @Test
    fun `observeCart delegates to dao`() = runTest {
        val items = listOf(sampleEntity())
        coEvery { dao.observeByUser("user-1") } returns flowOf(items)

        val result = dataSource.observeCart("user-1")
        val firstEmission = result.first()

        assertEquals(items, firstEmission)
        coVerify(exactly = 1) { dao.observeByUser("user-1") }
    }

    @Test
    fun `getCart delegates to dao`() = runTest {
        val items = listOf(sampleEntity())
        coEvery { dao.getByUser("user-1") } returns items

        val result = dataSource.getCart("user-1")

        assertEquals(items, result)
        coVerify(exactly = 1) { dao.getByUser("user-1") }
    }

    @Test
    fun `upsertAll delegates to dao`() = runTest {
        val items = listOf(sampleEntity())
        coEvery { dao.upsertAll(items) } returns listOf(1L)

        dataSource.upsertAll(items)

        coVerify(exactly = 1) { dao.upsertAll(items) }
    }

    @Test
    fun `deleteItem delegates to dao`() = runTest {
        coEvery { dao.deleteById("line-1", "user-1") } returns 1

        dataSource.deleteItem("line-1", "user-1")

        coVerify(exactly = 1) { dao.deleteById("line-1", "user-1") }
    }

    @Test
    fun `clearCart delegates to dao`() = runTest {
        coEvery { dao.clearForUser("user-1") } returns 3

        dataSource.clearCart("user-1")

        coVerify(exactly = 1) { dao.clearForUser("user-1") }
    }

    private fun sampleEntity() = CartItemEntity(
        id = "line-1",
        cartId = "cart-1",
        productId = "product-1",
        variantId = "variant-1",
        title = "Blue Hoodie",
        variantTitle = "Size M",
        price = 45.0,
        quantity = 1,
        maxQuantity = 3,
        imageUrl = "",
        userId = "user-1",
        currency = "USD",
        subtotalPrice = 45.0,
        totalPrice = 45.0,
        appliedCouponCode = null,
        appliedCouponApplicable = null,
        invalidationDate = System.currentTimeMillis(),
    )
}
