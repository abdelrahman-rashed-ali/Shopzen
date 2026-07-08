package shopzen.presentation.cart.screen

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import shopzen.presentation.R
import shopzen.presentation.cart.intent.CartIntent
import shopzen.presentation.cart.state.CartItemUi
import shopzen.presentation.cart.state.CartState
import shopzen.presentation.common.util.UiText
import shopzen.presentation.common.theme.ShopzenTheme

@RunWith(AndroidJUnit4::class)
class CartScreenContentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun loading_state_shows_loading_indicator() {
        composeRule.setContent {
            ShopzenTheme {
                CartScreenContent(
                    state = CartState(isLoading = true),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun error_state_shows_retry_action_and_dispatches_retry_intent() {
        val intents = mutableListOf<CartIntent>()

        composeRule.setContent {
            ShopzenTheme {
                CartScreenContent(
                    state = CartState(
                        error = UiText.DynamicString("Network down"),
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = { intents += it },
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Network down").assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_retry)).performClick()

        assertEquals(listOf(CartIntent.Retry), intents)
    }

    @Test
    fun empty_state_shows_start_shopping_action() {
        var backClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                CartScreenContent(
                    state = CartState(),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = {},
                    onNavigateBack = { backClicks++ },
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_start_shopping))
            .performClick()

        assertEquals(1, backClicks)
    }

    @Test
    fun filled_cart_wires_item_and_summary_intents() {
        val intents = mutableListOf<CartIntent>()
        val item = sampleItemUi()

        composeRule.setContent {
            ShopzenTheme {
                CartScreenContent(
                    state = CartState(
                        items = listOf(item),
                        formattedSubtotal = "$45.00",
                        formattedTotal = "$45.00",
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = { intents += it },
                    onNavigateBack = {},
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
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_add_coupon))
            .performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_checkout))
            .performClick()

        assertEquals(
            listOf(
                CartIntent.NavigateToProduct(item.productId),
                CartIntent.DecrementQuantity(item.id),
                CartIntent.IncrementQuantity(item.id),
                CartIntent.RequestRemoveItem(item.id),
                CartIntent.OpenCouponSheet,
                CartIntent.Checkout,
            ),
            intents,
        )
    }

    @Test
    fun remove_item_dialog_dispatches_confirm_and_dismiss_intents() {
        val intents = mutableListOf<CartIntent>()

        composeRule.setContent {
            ShopzenTheme {
                CartScreenContent(
                    state = CartState(
                        items = listOf(sampleItemUi()),
                        formattedSubtotal = "$45.00",
                        formattedTotal = "$45.00",
                        showRemoveItemDialog = true,
                        pendingRemovalItemId = "item-1",
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = { intents += it },
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_remove_item_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_remove))
            .performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_cancel))
            .performClick()

        assertEquals(
            listOf(
                CartIntent.ConfirmRemoveItem,
                CartIntent.DismissRemoveItemDialog,
            ),
            intents,
        )
    }

    @Test
    fun clear_cart_dialog_dispatches_confirm_and_dismiss_intents() {
        val intents = mutableListOf<CartIntent>()

        composeRule.setContent {
            ShopzenTheme {
                CartScreenContent(
                    state = CartState(
                        items = listOf(sampleItemUi()),
                        formattedSubtotal = "$45.00",
                        formattedTotal = "$45.00",
                        showClearCartDialog = true,
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = { intents += it },
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_clear_cart_title))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_clear_all))
            .performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_cancel))
            .performClick()

        assertEquals(
            listOf(
                CartIntent.ConfirmClearCart,
                CartIntent.DismissClearCartDialog,
            ),
            intents,
        )
    }

    @Test
    fun coupon_sheet_renders_from_screen_state() {
        composeRule.setContent {
            ShopzenTheme {
                CartScreenContent(
                    state = CartState(
                        showCouponSheet = true,
                    ),
                    snackbarHostState = SnackbarHostState(),
                    onIntent = {},
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.cart_add_coupon))
            .assertIsDisplayed()
    }

    private fun sampleItemUi() = CartItemUi(
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
