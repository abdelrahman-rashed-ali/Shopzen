package shopzen.presentation.cart.components

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import shopzen.presentation.R
import shopzen.presentation.common.theme.ShopzenTheme

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalMaterial3Api::class)
class CartComponentsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun cart_top_bar_shows_clear_action_when_cart_has_items() {
        var backClicks = 0
        var clearClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                CartTopBar(
                    hasItems = true,
                    onBack = { backClicks++ },
                    onClearCart = { clearClicks++ },
                )
            }
        }

        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_back))
            .performClick()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_clear_cart_cd))
            .performClick()

        assertEquals(1, backClicks)
        assertEquals(1, clearClicks)
    }

    @Test
    fun cart_top_bar_hides_clear_action_when_cart_is_empty() {
        composeRule.setContent {
            ShopzenTheme {
                CartTopBar(
                    hasItems = false,
                    onBack = {},
                    onClearCart = {},
                )
            }
        }

        composeRule.onAllNodesWithContentDescription(composeRule.activity.getString(R.string.cart_clear_cart_cd))
            .assertCountEquals(0)
    }

    @Test
    fun quantity_stepper_disables_edges_and_dispatches_clicks() {
        var incrementClicks = 0
        var decrementClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                QuantityStepper(
                    quantity = 2,
                    maxQuantity = 3,
                    onIncrement = { incrementClicks++ },
                    onDecrement = { decrementClicks++ },
                )
            }
        }

        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_decrease_qty))
            .performClick()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_increase_qty))
            .performClick()

        assertEquals(1, decrementClicks)
        assertEquals(1, incrementClicks)
    }

    @Test
    fun quantity_stepper_disables_decrement_at_minimum_and_increment_at_maximum() {
        composeRule.setContent {
            ShopzenTheme {
                QuantityStepper(
                    quantity = 1,
                    maxQuantity = 1,
                    onIncrement = {},
                    onDecrement = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_decrease_qty))
            .assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_increase_qty))
            .assertIsNotEnabled()
    }

    @Test
    fun cart_item_card_renders_details_and_dispatches_actions() {
        var incrementClicks = 0
        var decrementClicks = 0
        var deleteClicks = 0
        var cardClicks = 0
        val item = sampleCartItemUi()

        composeRule.setContent {
            ShopzenTheme {
                CartItemCard(
                    item = item,
                    onIncrement = { incrementClicks++ },
                    onDecrement = { decrementClicks++ },
                    onDelete = { deleteClicks++ },
                    onCardClick = { cardClicks++ },
                )
            }
        }

        composeRule.onNodeWithText(item.title).performClick()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_decrease_qty))
            .performClick()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_increase_qty))
            .performClick()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_remove_item_cd))
            .performClick()

        composeRule.onNodeWithText(item.title).assertIsDisplayed()
        composeRule.onNodeWithText(item.variantTitle).assertIsDisplayed()
        composeRule.onNodeWithText(item.formattedPrice).assertIsDisplayed()

        assertEquals(1, cardClicks)
        assertEquals(1, decrementClicks)
        assertEquals(1, incrementClicks)
        assertEquals(1, deleteClicks)
    }

    @Test
    fun cart_summary_section_shows_add_coupon_action_when_coupon_not_applied() {
        var addCouponClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                CartSummarySection(
                    formattedSubtotal = "$245.00",
                    formattedTotal = "$245.00",
                    formattedDiscount = null,
                    couponApplied = false,
                    appliedCouponLabel = null,
                    onAddCoupon = { addCouponClicks++ },
                    onRemoveCoupon = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_add_coupon))
            .performClick()

        assertEquals(1, addCouponClicks)
    }

    @Test
    fun cart_summary_section_shows_applied_coupon_and_remove_action() {
        var removeClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                CartSummarySection(
                    formattedSubtotal = "$245.00",
                    formattedTotal = "$220.50",
                    formattedDiscount = "-$24.50",
                    couponApplied = true,
                    appliedCouponLabel = "SAVE10 (-10%)",
                    onAddCoupon = {},
                    onRemoveCoupon = { removeClicks++ },
                )
            }
        }

        composeRule.onNodeWithText("SAVE10 (-10%)").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_remove_coupon_cd))
            .performClick()

        assertEquals(1, removeClicks)
    }

    @Test
    fun add_coupon_section_disables_apply_when_code_is_blank_and_shows_error() {
        var applyClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                AddCouponSection(
                    couponCode = "",
                    couponError = "Invalid coupon code",
                    couponApplied = false,
                    appliedCouponLabel = null,
                    isCouponLoading = false,
                    onCodeChange = {},
                    onApply = { applyClicks++ },
                    onRemove = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_apply))
            .assertIsNotEnabled()
        composeRule.onNodeWithText("Invalid coupon code").assertIsDisplayed()

        assertEquals(0, applyClicks)
    }

    @Test
    fun add_coupon_section_shows_applied_coupon_state_and_remove_action() {
        var removeClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                AddCouponSection(
                    couponCode = "SAVE10",
                    couponError = null,
                    couponApplied = true,
                    appliedCouponLabel = "SAVE10 (-10%)",
                    isCouponLoading = false,
                    onCodeChange = {},
                    onApply = {},
                    onRemove = { removeClicks++ },
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_coupon_applied))
            .assertIsDisplayed()
        composeRule.onNodeWithText("SAVE10 (-10%)").assertIsDisplayed()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_remove))
            .performClick()

        assertEquals(1, removeClicks)
    }

    @Test
    fun coupon_bottom_sheet_renders_when_visible_and_dispatches_dismiss() {
        var dismissClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                CouponBottomSheet(
                    visible = true,
                    couponCode = "SAVE10",
                    couponError = null,
                    isCouponLoading = false,
                    sheetState = sheetState,
                    onCodeChange = {},
                    onApply = {},
                    onDismiss = { dismissClicks++ },
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_add_coupon))
            .assertIsDisplayed()
        composeRule.onNodeWithContentDescription(composeRule.activity.getString(R.string.cart_close))
            .performClick()

        assertEquals(1, dismissClicks)
    }

    @Test
    fun checkout_button_dispatches_click() {
        var clicks = 0

        composeRule.setContent {
            ShopzenTheme {
                CheckoutButton(onClick = { clicks++ })
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_checkout))
            .performClick()

        assertEquals(1, clicks)
    }

    private fun sampleCartItemUi() = shopzen.presentation.cart.state.CartItemUi(
        id = "item-1",
        productId = "product-1",
        variantId = "variant-1",
        title = "Blue Hoodie",
        variantTitle = "Size M",
        formattedPrice = "$45.00",
        quantity = 2,
        maxQuantity = 3,
        imageUrl = "",
    )
}
