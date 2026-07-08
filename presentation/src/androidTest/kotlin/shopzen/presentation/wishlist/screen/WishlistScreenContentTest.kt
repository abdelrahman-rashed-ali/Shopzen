package shopzen.presentation.wishlist.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import shopzen.domain.wishlist.model.WishlistItem
import shopzen.presentation.common.theme.ShopzenTheme
import shopzen.presentation.wishlist.WishlistTestTags
import shopzen.presentation.wishlist.intent.WishlistIntent
import shopzen.presentation.wishlist.state.WishlistState

class WishlistScreenContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyWishlistShowsCollectionEmptyStateAndShopAction() {
        var shopClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                WishlistContent(
                    state = WishlistState(items = emptyList()),
                    onIntent = {},
                    onNavigateToProduct = {},
                    onNavigateToHome = { shopClicks++ },
                )
            }
        }

        composeRule.onNodeWithTag(WishlistTestTags.Header).assertIsDisplayed()
        composeRule.onNodeWithTag(WishlistTestTags.Empty).assertIsDisplayed()
        composeRule.onNodeWithText("Your wishlist is empty").assertIsDisplayed()
        composeRule.onNodeWithText("Shop Now").performClick()

        assertEquals(1, shopClicks)
    }

    @Test
    fun populatedWishlistShowsCountAndWiresItemActions() {
        val intents = mutableListOf<WishlistIntent>()
        var openedProduct: String? = null
        val items = sampleItems()

        composeRule.setContent {
            ShopzenTheme {
                WishlistContent(
                    state = WishlistState(items = items),
                    onIntent = { intents += it },
                    onNavigateToProduct = { openedProduct = it },
                    onNavigateToHome = {},
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("2 items").assertIsDisplayed()
        composeRule.onNodeWithTag(WishlistTestTags.item("wish-1")).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(WishlistTestTags.item("wish-1")).performClick()
        composeRule.onNodeWithTag(WishlistTestTags.remove("wish-1")).performClick()
        composeRule.onNodeWithTag(WishlistTestTags.addToCart("wish-1")).performClick()

        assertEquals("product-1", openedProduct)
        assertEquals(
            listOf(
                WishlistIntent.RequestRemoveItem("wish-1"),
                WishlistIntent.AddToCart("product-1"),
            ),
            intents,
        )
    }

    private fun sampleItems() = listOf(
        WishlistItem(
            id = "wish-1",
            productId = "product-1",
            title = "Obsidian Chronograph",
            vendor = "Obsidian",
            price = "4500",
            imageUrl = "https://example.com/watch.jpg",
            userId = "user-1",
            addedAt = 1L,
        ),
        WishlistItem(
            id = "wish-2",
            productId = "product-2",
            title = "Aethelgard Bracelet",
            vendor = "Maison Lumi",
            price = "1200",
            imageUrl = "https://example.com/bracelet.jpg",
            userId = "user-1",
            addedAt = 2L,
        ),
    )
}
