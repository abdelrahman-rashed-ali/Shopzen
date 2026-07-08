package shopzen.presentation.checkout.viewmodel

import android.util.Log
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import shopzen.domain.auth.model.User
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.cart.model.Cart
import shopzen.domain.cart.model.CartItem
import shopzen.domain.cart.model.DiscountCode
import shopzen.domain.cart.model.DiscountType
import shopzen.domain.cart.usecase.ClearCartUseCase
import shopzen.domain.cart.usecase.GetCartUseCase
import shopzen.domain.checkout.model.OrderConfirmation
import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.checkout.model.PaymobClientSecret
import shopzen.domain.checkout.model.PaymobIntentionId
import shopzen.domain.checkout.model.PaymobPaymentIntention
import shopzen.domain.checkout.model.PaymobPublicKey
import shopzen.domain.checkout.usecase.CreatePaymobPaymentIntentionUseCase
import shopzen.domain.checkout.usecase.GetAvailablePaymentMethodsUseCase
import shopzen.domain.checkout.usecase.PlaceOrderUseCase
import shopzen.domain.customer.usecase.GetCurrentShopifyCustomerIdUseCase
import shopzen.domain.profile.model.Address
import shopzen.domain.profile.usecase.GetSavedAddressesUseCase
import shopzen.presentation.MainDispatcherRule
import shopzen.presentation.R
import shopzen.presentation.checkout.intent.CheckoutIntent
import shopzen.presentation.common.util.UiText

class CheckoutViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCurrentUserUseCase: GetCurrentUserUseCase = mockk()
    private val getCartUseCase: GetCartUseCase = mockk()
    private val getCurrentShopifyCustomerIdUseCase: GetCurrentShopifyCustomerIdUseCase = mockk()
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase = mockk()
    private val getAvailablePaymentMethodsUseCase: GetAvailablePaymentMethodsUseCase = mockk()
    private val placeOrderUseCase: PlaceOrderUseCase = mockk()
    private val createPaymobPaymentIntentionUseCase: CreatePaymobPaymentIntentionUseCase = mockk()
    private val clearCartUseCase: ClearCartUseCase = mockk()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any<Throwable>()) } returns 0
        coEvery { getCurrentShopifyCustomerIdUseCase() } returns Result.success(123L)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `load checkout - populates summary with cart and address`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val cart = sampleCart()
        val address = sampleAddress(isDefault = true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(cart))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(45.0) } returns listOf(
            PaymentMethod.CASH_ON_DELIVERY,
            PaymentMethod.ONLINE_PAYMENT,
        )

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)

        assertEquals(1, viewModel.state.value.items.size)
        assertEquals(address.id, viewModel.state.value.selectedAddressId)
        assertEquals(PaymentMethod.CASH_ON_DELIVERY, viewModel.state.value.selectedPaymentMethod)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.error == null)
    }

    @Test
    fun `continue to payment without address sets inline error`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(emptyList()))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)

        viewModel.processIntent(CheckoutIntent.ContinueToPayment)

        assertEquals(
            R.string.checkout_error_address_required,
            (viewModel.state.value.error as UiText.StringResource).resId,
        )
    }

    @Test
    fun `request place order shows confirmation dialog when state valid`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)

        viewModel.processIntent(CheckoutIntent.RequestPlaceOrder)

        assertTrue(viewModel.state.value.showPlaceOrderDialog)
        assertTrue(viewModel.state.value.error == null)
    }

    @Test
    fun `confirm place order success clears cart and emits order confirmation effect`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.CASH_ON_DELIVERY)
        coEvery {
            placeOrderUseCase(any())
        } returns Result.success(OrderConfirmation(orderId = "order-1", orderNumber = "#1001"))
        coEvery { clearCartUseCase("user-1") } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)
        viewModel.processIntent(CheckoutIntent.RequestPlaceOrder)

        val effect = async { viewModel.effects.first() }
        viewModel.processIntent(CheckoutIntent.ConfirmPlaceOrder)
        awaitLoaded(viewModel)

        assertEquals(
            CheckoutEffect.NavigateToOrderConfirmation("order-1", "#1001"),
            effect.await(),
        )
        coVerify(exactly = 1) { placeOrderUseCase(any()) }
        coVerify(exactly = 1) { clearCartUseCase("user-1") }
        assertFalse(viewModel.state.value.isPlacingOrder)
    }

    @Test
    fun `confirm place order with online payment starts Paymob before placing Shopify order`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        val intention = PaymobPaymentIntention(
            clientSecret = PaymobClientSecret("client-secret"),
            publicKey = PaymobPublicKey("public-key"),
            intentionId = PaymobIntentionId("intent-1"),
            intentionOrderId = 123L,
        )
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)
        coEvery { createPaymobPaymentIntentionUseCase(any()) } returns Result.success(intention)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)
        viewModel.processIntent(CheckoutIntent.RequestPlaceOrder)

        viewModel.processIntent(CheckoutIntent.ConfirmPlaceOrder)
        awaitPaymobLaunch(viewModel)

        assertEquals(
            PaymobIntentionId("intent-1"),
            viewModel.state.value.pendingPaymobLaunch?.intentionId,
        )
        assertEquals(
            PaymobClientSecret("client-secret"),
            viewModel.state.value.pendingPaymobLaunch?.clientSecret,
        )
        assertEquals(
            PaymobPublicKey("public-key"),
            viewModel.state.value.pendingPaymobLaunch?.publicKey,
        )
        assertTrue(viewModel.state.value.isOnlinePaymentInProgress)
        coVerify(exactly = 1) { createPaymobPaymentIntentionUseCase(any()) }
        coVerify(exactly = 0) { placeOrderUseCase(any()) }
    }

    @Test
    fun `duplicate confirm place order starts Paymob once`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        val intention = PaymobPaymentIntention(
            clientSecret = PaymobClientSecret("client-secret"),
            publicKey = PaymobPublicKey("public-key"),
            intentionId = PaymobIntentionId("intent-1"),
            intentionOrderId = 123L,
        )
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)
        coEvery { createPaymobPaymentIntentionUseCase(any()) } returns Result.success(intention)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)
        viewModel.processIntent(CheckoutIntent.RequestPlaceOrder)

        viewModel.processIntent(CheckoutIntent.ConfirmPlaceOrder)
        viewModel.processIntent(CheckoutIntent.ConfirmPlaceOrder)
        awaitPaymobLaunch(viewModel)

        assertEquals(
            PaymobIntentionId("intent-1"),
            viewModel.state.value.pendingPaymobLaunch?.intentionId,
        )
        coVerify(exactly = 1) { createPaymobPaymentIntentionUseCase(any()) }
        coVerify(exactly = 0) { placeOrderUseCase(any()) }
        assertTrue(viewModel.state.value.isOnlinePaymentInProgress)
        assertFalse(viewModel.state.value.showPlaceOrderDialog)
    }

    @Test
    fun `consume pending Paymob launch clears only matching launch`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        val intention = PaymobPaymentIntention(
            clientSecret = PaymobClientSecret("client-secret"),
            publicKey = PaymobPublicKey("public-key"),
            intentionId = PaymobIntentionId("intent-1"),
            intentionOrderId = 123L,
        )
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)
        coEvery { createPaymobPaymentIntentionUseCase(any()) } returns Result.success(intention)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)
        viewModel.processIntent(CheckoutIntent.RequestPlaceOrder)
        viewModel.processIntent(CheckoutIntent.ConfirmPlaceOrder)
        awaitPaymobLaunch(viewModel)

        viewModel.processIntent(
            CheckoutIntent.ConsumePendingPaymobLaunch(PaymobIntentionId("other-intent"))
        )
        assertEquals(
            PaymobIntentionId("intent-1"),
            viewModel.state.value.pendingPaymobLaunch?.intentionId,
        )

        viewModel.processIntent(
            CheckoutIntent.ConsumePendingPaymobLaunch(PaymobIntentionId("intent-1"))
        )
        assertEquals(null, viewModel.state.value.pendingPaymobLaunch)
    }

    @Test
    fun `online payment success places order and clears cart`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)
        coEvery {
            placeOrderUseCase(any())
        } returns Result.success(OrderConfirmation(orderId = "order-1", orderNumber = "#1001"))
        coEvery { clearCartUseCase("user-1") } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)

        val effect = async { viewModel.effects.first() }
        viewModel.processIntent(CheckoutIntent.OnlinePaymentSucceeded(mapOf("success" to "true")))
        awaitLoaded(viewModel)

        assertEquals(
            CheckoutEffect.NavigateToOrderConfirmation("order-1", "#1001"),
            effect.await(),
        )
        coVerify(exactly = 1) { placeOrderUseCase(any()) }
        coVerify(exactly = 1) { clearCartUseCase("user-1") }
        assertFalse(viewModel.state.value.isOnlinePaymentInProgress)
        assertFalse(viewModel.state.value.isPlacingOrder)
    }

    @Test
    fun `online payment failure callback with approved Paymob url places order`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)
        coEvery {
            placeOrderUseCase(any())
        } returns Result.success(OrderConfirmation(orderId = "order-1", orderNumber = "#1001"))
        coEvery { clearCartUseCase("user-1") } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)

        val effect = async { viewModel.effects.first() }
        viewModel.processIntent(
            CheckoutIntent.OnlinePaymentFailed(
                "Payment failed: https://accept.paymob.com/callback?success=true&pending=false",
            )
        )
        awaitLoaded(viewModel)

        assertEquals(
            CheckoutEffect.NavigateToOrderConfirmation("order-1", "#1001"),
            effect.await(),
        )
        coVerify(exactly = 1) { placeOrderUseCase(any()) }
        coVerify(exactly = 1) { clearCartUseCase("user-1") }
        assertTrue(viewModel.state.value.error == null)
    }

    @Test
    fun `online payment failure callback with Paymob JSON success places order`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)
        coEvery {
            placeOrderUseCase(any())
        } returns Result.success(OrderConfirmation(orderId = "order-1", orderNumber = "#1001"))
        coEvery { clearCartUseCase("user-1") } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)

        val effect = async { viewModel.effects.first() }
        viewModel.processIntent(
            CheckoutIntent.OnlinePaymentFailed(
                """Payment failed: {"success":"true","pending":"false","txn_response_code":"APPROVED"}""",
            )
        )
        awaitLoaded(viewModel)

        assertEquals(
            CheckoutEffect.NavigateToOrderConfirmation("order-1", "#1001"),
            effect.await(),
        )
        coVerify(exactly = 1) { placeOrderUseCase(any()) }
        coVerify(exactly = 1) { clearCartUseCase("user-1") }
        assertTrue(viewModel.state.value.error == null)
    }

    @Test
    fun `online payment failure callback with Paymob JSON failure does not place order`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)

        viewModel.processIntent(
            CheckoutIntent.OnlinePaymentFailed(
                """{"success":"false","pending":"false","data":{"message":"Declined"}}""",
            )
        )

        coVerify(exactly = 0) { placeOrderUseCase(any()) }
        coVerify(exactly = 0) { clearCartUseCase("user-1") }
        assertFalse(viewModel.state.value.isOnlinePaymentInProgress)
        assertFalse(viewModel.state.value.isPlacingOrder)
    }

    @Test
    fun `online payment failure callback with Paymob pending shows pending and does not place order`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)

        viewModel.processIntent(
            CheckoutIntent.OnlinePaymentFailed(
                """{"success":"false","pending":"true","data":{"message":"Pending"}}""",
            )
        )

        assertEquals(
            R.string.checkout_payment_pending,
            (viewModel.state.value.error as UiText.StringResource).resId,
        )
        coVerify(exactly = 0) { placeOrderUseCase(any()) }
        coVerify(exactly = 0) { clearCartUseCase("user-1") }
        assertFalse(viewModel.state.value.isOnlinePaymentInProgress)
        assertFalse(viewModel.state.value.isPlacingOrder)
    }

    @Test
    fun `blank online payment failure while payment in progress does not place order`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        val address = sampleAddress(isDefault = true)
        val intention = PaymobPaymentIntention(
            clientSecret = PaymobClientSecret("client-secret"),
            publicKey = PaymobPublicKey("public-key"),
            intentionId = PaymobIntentionId("intent-1"),
            intentionOrderId = 123L,
        )
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(listOf(address)))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)
        coEvery { createPaymobPaymentIntentionUseCase(any()) } returns Result.success(intention)
        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)
        viewModel.processIntent(CheckoutIntent.RequestPlaceOrder)
        viewModel.processIntent(CheckoutIntent.ConfirmPlaceOrder)
        awaitPaymobLaunch(viewModel)

        viewModel.processIntent(CheckoutIntent.OnlinePaymentFailed(null))

        assertEquals(
            R.string.checkout_error_payment_failed,
            (viewModel.state.value.error as UiText.StringResource).resId,
        )
        coVerify(exactly = 0) { placeOrderUseCase(any()) }
        coVerify(exactly = 0) { clearCartUseCase("user-1") }
        assertFalse(viewModel.state.value.isOnlinePaymentInProgress)
        assertFalse(viewModel.state.value.isPlacingOrder)
    }

    @Test
    fun `add address clicked emits navigation effect`() = runTest {
        val user = User("user-1", "ada@example.com", "Ada", null, true)
        coEvery { getCurrentUserUseCase() } returns Result.success(user)
        every { getCartUseCase("user-1") } returns flowOf(Result.success(sampleCart()))
        every { getSavedAddressesUseCase("user-1") } returns flowOf(Result.success(emptyList()))
        every { getAvailablePaymentMethodsUseCase(any()) } returns listOf(PaymentMethod.ONLINE_PAYMENT)

        val viewModel = createViewModel()
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
        awaitLoaded(viewModel)

        val effect = async { viewModel.effects.first() }
        viewModel.processIntent(CheckoutIntent.AddAddressClicked)
        assertEquals(CheckoutEffect.NavigateToAddAddress, effect.await())
    }

    private fun createViewModel() = CheckoutViewModel(
        getCurrentUserUseCase = getCurrentUserUseCase,
        getCartUseCase = getCartUseCase,
        getCurrentShopifyCustomerIdUseCase = getCurrentShopifyCustomerIdUseCase,
        getSavedAddressesUseCase = getSavedAddressesUseCase,
        getAvailablePaymentMethodsUseCase = getAvailablePaymentMethodsUseCase,
        placeOrderUseCase = placeOrderUseCase,
        createPaymobPaymentIntentionUseCase = createPaymobPaymentIntentionUseCase,
        clearCartUseCase = clearCartUseCase,
    )

    private suspend fun awaitLoaded(viewModel: CheckoutViewModel) {
        withTimeout(2_000) {
            while (viewModel.state.value.isLoading || viewModel.state.value.items.isEmpty()) {
                yield()
            }
        }
    }

    private suspend fun awaitPaymobLaunch(viewModel: CheckoutViewModel) {
        withTimeout(2_000) {
            while (viewModel.state.value.pendingPaymobLaunch == null) {
                yield()
            }
        }
    }

    private fun sampleCart() = Cart(
        items = listOf(sampleItem()),
        currency = "USD",
        subtotalPrice = 45.0,
        discountAmount = 0.0,
        totalPrice = 45.0,
        appliedCoupon = DiscountCode("SAVE10", DiscountType.PERCENTAGE, 10.0),
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

    private fun sampleAddress(isDefault: Boolean = false) = Address(
        id = "address-1",
        recipientName = "Ada Lovelace",
        addressLine1 = "1 Main St",
        city = "Cairo",
        postalCode = "12345",
        country = "Egypt",
        phone = "+201000000000",
        isDefault = isDefault,
    )
}
