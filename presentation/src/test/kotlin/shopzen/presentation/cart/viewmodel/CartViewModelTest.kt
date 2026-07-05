package shopzen.presentation.cart.viewmodel

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import shopzen.domain.auth.model.User
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.cart.model.Cart
import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.model.CouponValidationResult
import shopzen.domain.cart.model.DiscountCode
import shopzen.domain.cart.model.DiscountType
import shopzen.domain.cart.usecase.AddToCartUseCase
import shopzen.domain.cart.usecase.ApplyCouponUseCase
import shopzen.domain.cart.usecase.ClearCartUseCase
import shopzen.domain.cart.usecase.GetCartTotalUseCase
import shopzen.domain.cart.usecase.GetCartUseCase
import shopzen.domain.cart.usecase.GetCurrencySymbolUseCase
import shopzen.domain.cart.usecase.RemoveCouponUseCase
import shopzen.domain.cart.usecase.RemoveFromCartUseCase
import shopzen.domain.cart.usecase.UpdateCartItemQuantityUseCase
import shopzen.domain.cart.usecase.ValidateCouponUseCase
import shopzen.presentation.MainDispatcherRule
import shopzen.presentation.R
import shopzen.presentation.cart.intent.CartIntent
import shopzen.presentation.common.util.UiText

class CartViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCartUseCase: GetCartUseCase = mockk()
    private val removeFromCartUseCase: RemoveFromCartUseCase = mockk()
    private val updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase = mockk()
    private val clearCartUseCase: ClearCartUseCase = mockk()
    private val getCartTotalUseCase: GetCartTotalUseCase = mockk()
    private val validateCouponUseCase: ValidateCouponUseCase = mockk()
    private val applyCouponUseCase: ApplyCouponUseCase = mockk()
    private val removeCouponUseCase: RemoveCouponUseCase = mockk()
    private val getCurrencySymbolUseCase: GetCurrencySymbolUseCase = mockk()
    private val getCurrentUserUseCase: GetCurrentUserUseCase = mockk()
    private val addToCartUseCase: AddToCartUseCase = mockk()

    @Test
    fun `init - when no user - emits not authenticated error`() = runTest {
        coEvery { getCurrentUserUseCase() } returns Result.success(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(R.string.cart_error_not_authenticated, (viewModel.state.value.error as UiText.StringResource).resId)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun `init - when user and cart exist - loads currency and cart`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val cart = sampleCart()
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        coEvery { getCurrencySymbolUseCase() } returns "USD"
        every { getCartUseCase("user-1", false) } returns flowOf(Result.success(cart))
        every { getCartTotalUseCase(any()) } returns 45.0

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.items.size)
        assertEquals("USD45.00", viewModel.state.value.formattedSubtotal)
        assertEquals("USD45.00", viewModel.state.value.formattedTotal)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.error == null)
    }

    @Test
    fun `request remove item then dismiss clears dialog state`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        coEvery { getCurrencySymbolUseCase() } returns "USD"
        every { getCartUseCase("user-1", false) } returns flowOf(Result.success(sampleCart()))
        every { getCartTotalUseCase(any()) } returns 45.0

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processIntent(CartIntent.RequestRemoveItem("line-1"))
        assertTrue(viewModel.state.value.showRemoveItemDialog)
        assertEquals("line-1", viewModel.state.value.pendingRemovalItemId)

        viewModel.processIntent(CartIntent.DismissRemoveItemDialog)
        assertFalse(viewModel.state.value.showRemoveItemDialog)
        assertEquals(null, viewModel.state.value.pendingRemovalItemId)
    }

    @Test
    fun `confirm clear cart clears items and calls use case`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        coEvery { getCurrencySymbolUseCase() } returns "USD"
        every { getCartUseCase("user-1", false) } returns flowOf(Result.success(sampleCart()))
        every { getCartTotalUseCase(any()) } returns 45.0
        coEvery { clearCartUseCase("user-1") } returns Result.success(Unit)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processIntent(CartIntent.RequestClearCart)
        viewModel.processIntent(CartIntent.ConfirmClearCart)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.showClearCartDialog)
        assertTrue(viewModel.state.value.items.isEmpty())
        coVerify(exactly = 1) { clearCartUseCase("user-1") }
    }

    @Test
    fun `add to cart when unauthenticated emits login effect`() = runTest {
        coEvery { getCurrentUserUseCase() } returns Result.success(null)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val effect = async { viewModel.effects.first() }
        viewModel.processIntent(
            CartIntent.AddToCart(
                productId = "product-1",
                variantId = "variant-1",
                title = "Blue Hoodie",
                variantTitle = "Size M",
                price = 45.0,
                maxQuantity = 3,
                imageUrl = "",
            )
        )

        assertEquals(CartEffect.NavigateToLogin, effect.await())
    }

    @Test
    fun `apply coupon with blank code sets inline error`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        coEvery { getCurrencySymbolUseCase() } returns "USD"
        every { getCartUseCase("user-1", false) } returns flowOf(Result.success(sampleCart()))
        every { getCartTotalUseCase(any()) } returns 45.0

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processIntent(CartIntent.UpdateCouponCode("   "))
        viewModel.processIntent(CartIntent.ApplyCoupon)

        assertEquals(R.string.cart_coupon_empty_error, (viewModel.state.value.couponError as UiText.StringResource).resId)
    }

    private fun createViewModel() = CartViewModel(
        getCartUseCase = getCartUseCase,
        removeFromCartUseCase = removeFromCartUseCase,
        updateCartItemQuantityUseCase = updateCartItemQuantityUseCase,
        clearCartUseCase = clearCartUseCase,
        getCartTotalUseCase = getCartTotalUseCase,
        validateCouponUseCase = validateCouponUseCase,
        applyCouponUseCase = applyCouponUseCase,
        removeCouponUseCase = removeCouponUseCase,
        getCurrencySymbolUseCase = getCurrencySymbolUseCase,
        getCurrentUserUseCase = getCurrentUserUseCase,
        addToCartUseCase = addToCartUseCase,
    )

    private fun sampleCart() = Cart(
        items = listOf(sampleItem()),
        currency = "USD",
        subtotalPrice = 45.0,
        discountAmount = 0.0,
        totalPrice = 45.0,
        appliedCoupon = null,
        userId = "user-1",
    )

    private fun sampleItem() = CartItem(
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
}
