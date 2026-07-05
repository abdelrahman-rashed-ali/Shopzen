package shopzen.data.cart.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import shopzen.data.cart.local.CartLocalDataSource
import shopzen.data.cart.local.entity.CartItemEntity
import shopzen.data.cart.remote.CartRemoteDataSource
import shopzen.data.cart.remote.CartSyncAdapter
import shopzen.data.cart.remote.CouponRemoteDataSource
import shopzen.data.cart.remote.DiscountCodeDto
import shopzen.data.cart.remote.PriceRuleDto
import shopzen.data.cart.remote.dto.CartCostDto
import shopzen.data.cart.remote.dto.CartDto
import shopzen.data.cart.remote.dto.CartLineCostDto
import shopzen.data.cart.remote.dto.CartLineDto
import shopzen.data.cart.remote.dto.CartLineEdgeDto
import shopzen.data.cart.remote.dto.CartLinesConnectionDto
import shopzen.data.cart.remote.dto.ImageRefDto
import shopzen.data.cart.remote.dto.MerchandiseDto
import shopzen.data.cart.remote.dto.MoneyDto
import shopzen.data.cart.remote.dto.ProductRefDto
import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.model.CouponValidationResult

class CartRepositoryImplTest {

    private lateinit var remote: CartRemoteDataSource
    private lateinit var local: CartLocalDataSource
    private lateinit var syncAdapter: CartSyncAdapter
    private lateinit var couponRemote: CouponRemoteDataSource
    private lateinit var repository: CartRepositoryImpl

    @Before
    fun setUp() {
        remote = mockk()
        local = mockk()
        syncAdapter = mockk()
        couponRemote = mockk()
        repository = CartRepositoryImpl(remote, local, syncAdapter, couponRemote)
    }

    @Test
    fun `addToCart creates cart when no cart id exists and upserts refreshed cart`() = runTest {
        val item = sampleCartItem()
        val refreshedCart = sampleCartDto(id = "cart-remote")
        coEvery { local.getCart("user-1") } returns emptyList()
        coEvery { syncAdapter.getCartId("user-1") } returns null
        coEvery { remote.createCart(item.variantId, item.quantity) } returns refreshedCart
        coEvery { syncAdapter.saveCartId("user-1", "cart-remote") } returns Unit
        coEvery { local.clearCart("user-1") } returns 0
        coEvery { local.upsertAll(any()) } returns emptyList()

        val result = repository.addToCart(item)

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) { remote.createCart(item.variantId, item.quantity) }
        coVerify(exactly = 1) { syncAdapter.saveCartId("user-1", "cart-remote") }
        coVerify(exactly = 1) { local.clearCart("user-1") }
        coVerify(exactly = 1) { local.upsertAll(any()) }
    }

    @Test
    fun `removeFromCart uses cached cart id and refreshes local snapshot`() = runTest {
        coEvery { local.getCart("user-1") } returns listOf(sampleEntity())
        coEvery { remote.removeLine("cart-1", "line-1") } returns sampleCartDto(id = "cart-1")
        coEvery { local.clearCart("user-1") } returns 0
        coEvery { local.upsertAll(any()) } returns emptyList()

        val result = repository.removeFromCart("line-1", "user-1")

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) { remote.removeLine("cart-1", "line-1") }
        coVerify(exactly = 1) { local.clearCart("user-1") }
    }

    @Test
    fun `validateCoupon returns valid percentage coupon when remote code matches`() = runTest {
        coEvery { couponRemote.getPriceRules() } returns listOf(
            PriceRuleDto(
                id = 1,
                title = "SAVE20",
                target_type = "line_item",
                value_type = "percentage",
                value = "20.0",
            )
        )
        coEvery { couponRemote.getDiscountCodes(1) } returns listOf(
            shopzen.data.cart.remote.DiscountCodeDto(
                id = 11,
                price_rule_id = 1,
                code = "SAVE20",
            )
        )

        val result = repository.validateCoupon(" save20 ")

        val coupon = result.getOrNull()
        assertEquals(CouponValidationResult.Valid("SAVE20", 20.0, null), coupon)
    }

    @Test
    fun `validateCoupon returns invalid for blank code without remote call`() = runTest {
        val result = repository.validateCoupon("   ")

        assertEquals(CouponValidationResult.Invalid("Coupon code is required"), result.getOrNull())
        coVerify(exactly = 0) { couponRemote.getPriceRules() }
    }

    @Test
    fun `applyCoupon delegates to remote and local refresh`() = runTest {
        coEvery { local.getCart("user-1") } returns listOf(sampleEntity())
        coEvery { remote.updateDiscountCodes("cart-1", listOf("SAVE10")) } returns sampleCartDto(id = "cart-1")
        coEvery { local.clearCart("user-1") } returns 0
        coEvery { local.upsertAll(any()) } returns emptyList()

        val result = repository.applyCoupon("user-1", "SAVE10")

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) { remote.updateDiscountCodes("cart-1", listOf("SAVE10")) }
    }

    private fun sampleCartItem() = CartItem(
        id = "line-1",
        productId = "product-1",
        variantId = "variant-1",
        title = "Blue Hoodie",
        variantTitle = "Size M",
        price = 45.0,
        quantity = 1,
        maxQuantity = 3,
        imageUrl = "",
        userId = "user-1",
    )

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

    private fun sampleCartDto(id: String) = CartDto(
        id = id,
        lines = CartLinesConnectionDto(
            edges = listOf(
                CartLineEdgeDto(
                    node = CartLineDto(
                        id = "line-1",
                        quantity = 1,
                        merchandise = MerchandiseDto(
                            id = "variant-1",
                            title = "Size M",
                            product = ProductRefDto(
                                id = "product-1",
                                title = "Blue Hoodie",
                            ),
                            image = ImageRefDto(url = "https://example.com/hoodie.png"),
                            quantityAvailable = 3,
                        ),
                        cost = CartLineCostDto(
                            totalAmount = MoneyDto(amount = "45.0", currencyCode = "USD"),
                        ),
                    ),
                )
            )
        ),
        cost = CartCostDto(
            subtotalAmount = MoneyDto(amount = "45.0", currencyCode = "USD"),
            totalAmount = MoneyDto(amount = "45.0", currencyCode = "USD"),
        ),
        discountCodes = emptyList(),
    )
}
