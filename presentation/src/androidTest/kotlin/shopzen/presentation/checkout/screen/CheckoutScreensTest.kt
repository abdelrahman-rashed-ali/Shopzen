package shopzen.presentation.checkout.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.profile.model.Address
import shopzen.presentation.R
import shopzen.presentation.checkout.intent.CheckoutIntent
import shopzen.presentation.checkout.state.CheckoutItemUi
import shopzen.presentation.checkout.state.CheckoutState
import shopzen.presentation.common.util.UiText
import shopzen.presentation.common.theme.ShopzenTheme

@RunWith(AndroidJUnit4::class)
class CheckoutScreensTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun checkout_summary_loading_state_shows_loading_message() {
        composeRule.setContent {
            ShopzenTheme {
                CheckoutSummaryContent(
                    state = CheckoutState(isLoading = true),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_loading))
            .assertIsDisplayed()
    }

    @Test
    fun checkout_summary_content_wires_address_and_continue_intents() {
        val intents = mutableListOf<CheckoutIntent>()
        val address = sampleAddress()
        val item = sampleCheckoutItem()

        composeRule.setContent {
            ShopzenTheme {
                CheckoutSummaryContent(
                    state = CheckoutState(
                        items = listOf(item),
                        addresses = listOf(address),
                        selectedAddressId = address.id,
                        formattedSubtotal = "$45.00",
                        formattedTotal = "$45.00",
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = { intents += it },
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithText(item.title).assertIsDisplayed()
        composeRule.onNodeWithText(address.recipientName).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_add_address))
            .performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_continue_to_payment))
            .performClick()

        assertEquals(
            listOf(
                CheckoutIntent.SelectShippingAddress(address.id),
                CheckoutIntent.AddAddressClicked,
                CheckoutIntent.ContinueToPayment,
            ),
            intents,
        )
    }

    @Test
    fun checkout_summary_error_state_shows_retry_action() {
        val intents = mutableListOf<CheckoutIntent>()

        composeRule.setContent {
            ShopzenTheme {
                CheckoutSummaryContent(
                    state = CheckoutState(
                        error = UiText.DynamicString("Checkout failed"),
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = { intents += it },
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Checkout failed").assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_retry))
            .performClick()

        assertEquals(listOf(CheckoutIntent.Retry), intents)
    }

    @Test
    fun payment_content_wires_payment_selection_and_place_order_request() {
        val intents = mutableListOf<CheckoutIntent>()
        val address = sampleAddress()

        composeRule.setContent {
            ShopzenTheme {
                PaymentContent(
                    state = CheckoutState(
                        items = listOf(sampleCheckoutItem()),
                        addresses = listOf(address),
                        selectedAddressId = address.id,
                        availablePaymentMethods = listOf(
                            PaymentMethod.CASH_ON_DELIVERY,
                            PaymentMethod.ONLINE_PAYMENT,
                        ),
                        selectedPaymentMethod = PaymentMethod.ONLINE_PAYMENT,
                        formattedSubtotal = "$45.00",
                        formattedTotal = "$45.00",
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = { intents += it },
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.checkout_payment_cash_on_delivery),
        ).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_place_order))
            .performClick()

        assertEquals(
            listOf(
                CheckoutIntent.SelectPaymentMethod(PaymentMethod.CASH_ON_DELIVERY),
                CheckoutIntent.RequestPlaceOrder,
            ),
            intents,
        )
    }

    @Test
    fun payment_dialog_wires_confirm_and_dismiss_intents() {
        val intents = mutableListOf<CheckoutIntent>()
        val address = sampleAddress()

        composeRule.setContent {
            ShopzenTheme {
                PaymentContent(
                    state = CheckoutState(
                        items = listOf(sampleCheckoutItem()),
                        addresses = listOf(address),
                        selectedAddressId = address.id,
                        availablePaymentMethods = listOf(PaymentMethod.ONLINE_PAYMENT),
                        selectedPaymentMethod = PaymentMethod.ONLINE_PAYMENT,
                        formattedSubtotal = "$45.00",
                        formattedTotal = "$45.00",
                        showPlaceOrderDialog = true,
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = { intents += it },
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_confirm_order_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_confirm_order_confirm))
            .performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_confirm_order_cancel))
            .performClick()

        assertEquals(
            listOf(
                CheckoutIntent.ConfirmPlaceOrder,
                CheckoutIntent.DismissPlaceOrderDialog,
            ),
            intents,
        )
    }

    @Test
    fun order_confirmation_screen_falls_back_to_order_id_when_number_blank() {
        var continueClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                OrderConfirmationScreen(
                    orderId = "order-123",
                    orderNumber = "",
                    onNavigateBack = {},
                    onContinueShopping = { continueClicks++ },
                )
            }
        }

        composeRule.onNodeWithText("order-123").assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_continue_shopping))
            .performClick()

        assertEquals(1, continueClicks)
    }

    private fun sampleCheckoutItem() = CheckoutItemUi(
        id = "item-1",
        title = "Blue Hoodie",
        variantTitle = "Size M",
        formattedUnitPrice = "$45.00",
        quantity = 1,
        formattedLineTotal = "$45.00",
        imageUrl = "",
    )

    private fun sampleAddress() = Address(
        id = "address-1",
        recipientName = "Ada Lovelace",
        addressLine1 = "1 Main St",
        city = "Cairo",
        postalCode = "12345",
        country = "Egypt",
        phone = "+201000000000",
    )
}
