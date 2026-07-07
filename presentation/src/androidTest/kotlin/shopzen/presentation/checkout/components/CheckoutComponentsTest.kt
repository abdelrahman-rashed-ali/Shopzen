package shopzen.presentation.checkout.components

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
import shopzen.presentation.checkout.state.CheckoutItemUi
import shopzen.presentation.theme.ShopzenTheme

@RunWith(AndroidJUnit4::class)
class CheckoutComponentsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun checkout_top_bar_dispatches_back_click() {
        var backClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                CheckoutTopBar(
                    title = "Checkout",
                    onNavigateBack = { backClicks++ },
                )
            }
        }

        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.checkout_back_cd))
            .performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun checkout_section_renders_title_and_trailing_content() {
        composeRule.setContent {
            ShopzenTheme {
                CheckoutSection(
                    titleContent = { Text("Section title") },
                    trailingContent = { Text("Action") },
                ) {
                    Text("Section body")
                }
            }
        }

        composeRule.onNodeWithText("Section title").assertIsDisplayed()
        composeRule.onNodeWithText("Action").assertIsDisplayed()
        composeRule.onNodeWithText("Section body").assertIsDisplayed()
    }

    @Test
    fun checkout_item_row_renders_item_details() {
        val item = sampleCheckoutItem()

        composeRule.setContent {
            ShopzenTheme {
                CheckoutItemRow(item = item)
            }
        }

        composeRule.onNodeWithText(item.title).assertIsDisplayed()
        composeRule.onNodeWithText(item.variantTitle).assertIsDisplayed()
        composeRule.onNodeWithText(item.formattedUnitPrice).assertIsDisplayed()
        composeRule.onNodeWithText(item.formattedLineTotal).assertIsDisplayed()
    }

    @Test
    fun price_breakdown_shows_coupon_and_discount_rows_when_present() {
        composeRule.setContent {
            ShopzenTheme {
                PriceBreakdown(
                    formattedSubtotal = "$245.00",
                    formattedDiscount = "-$24.50",
                    formattedTotal = "$220.50",
                    appliedCouponLabel = "SAVE10",
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_subtotal))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_applied_coupon))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_discount))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_total))
            .assertIsDisplayed()
    }

    @Test
    fun address_choice_card_dispatches_click_and_shows_selection() {
        var clicks = 0
        val address = sampleAddress()

        composeRule.setContent {
            ShopzenTheme {
                AddressChoiceCard(
                    address = address,
                    selected = true,
                    onClick = { clicks++ },
                )
            }
        }

        composeRule.onNodeWithText(address.recipientName).performClick()
        composeRule.onNodeWithText(address.recipientName).assertIsDisplayed()

        assertEquals(1, clicks)
    }

    @Test
    fun payment_method_row_dispatches_click_and_shows_selection() {
        var clicks = 0

        composeRule.setContent {
            ShopzenTheme {
                PaymentMethodRow(
                    method = PaymentMethod.CASH_ON_DELIVERY,
                    selected = true,
                    onClick = { clicks++ },
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.checkout_payment_cash_on_delivery),
        ).performClick()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.checkout_payment_cash_on_delivery_desc),
        ).assertIsDisplayed()

        assertEquals(1, clicks)
    }

    @Test
    fun order_success_content_renders_order_number_and_continue_action() {
        var continueClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                OrderSuccessContent(
                    orderNumber = "#1001",
                    onContinueShopping = { continueClicks++ },
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_order_success_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText("#1001").assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.checkout_continue_shopping))
            .performClick()

        assertEquals(1, continueClicks)
    }

    private fun sampleCheckoutItem() = CheckoutItemUi(
        id = "item-1",
        title = "Blue Hoodie",
        variantTitle = "Size M",
        formattedUnitPrice = "$45.00",
        quantity = 2,
        formattedLineTotal = "$90.00",
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
