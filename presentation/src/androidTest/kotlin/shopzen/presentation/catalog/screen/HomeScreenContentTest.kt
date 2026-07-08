package shopzen.presentation.catalog.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import shopzen.domain.catalog.model.Brand
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.presentation.catalog.state.HomeState
import shopzen.presentation.theme.ShopzenTheme

class HomeScreenContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeContentRendersAllEditorialSections() {
        composeRule.setContent {
            ShopzenTheme {
                HomeContent(
                    state = sampleHomeState(),
                    wishlistProductIds = setOf("product-2"),
                    onNavigateToBrand = {},
                    onNavigateToCategory = {},
                    onNavigateToProduct = {},
                    onWishlistClick = {},
                    onNavigateToProducts = {},
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(HomeTestTags.Hero).assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTestTags.Brands).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTestTags.Categories).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(HomeTestTags.NewArrivals).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Featured Brands").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Curations").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("New Arrivals").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun homeContentWiresPrimaryNavigationCallbacks() {
        var openedBrand: String? = null
        var openedCategory: String? = null
        var viewAllClicks = 0

        composeRule.setContent {
            ShopzenTheme {
                HomeContent(
                    state = sampleHomeState(),
                    wishlistProductIds = emptySet(),
                    onNavigateToBrand = { openedBrand = it },
                    onNavigateToCategory = { openedCategory = it },
                    onNavigateToProduct = {},
                    onWishlistClick = {},
                    onNavigateToProducts = { viewAllClicks++ },
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Explore Collection").performClick()
        composeRule.onNodeWithText("Maison Lumi").performScrollTo().performClick()
        composeRule.onNodeWithText("TIMEPIECES").performScrollTo().performClick()

        assertEquals(1, viewAllClicks)
        assertEquals("Maison Lumi", openedBrand)
        assertEquals("watches", openedCategory)
    }

    private fun sampleHomeState() = HomeState(
        bannerImages = listOf("https://example.com/banner.jpg"),
        brands = listOf(
            Brand(name = "Maison Lumi"),
            Brand(name = "Obsidian"),
        ),
        categories = listOf(
            Category(
                id = "watches",
                title = "Timepieces",
                imageUrl = "https://example.com/watches.jpg",
            ),
            Category(
                id = "rings",
                title = "Rings",
                imageUrl = "https://example.com/rings.jpg",
            ),
        ),
        newArrivals = listOf(
            Product(
                id = "product-1",
                title = "Aethelgard Ring",
                vendor = "Maison Lumi",
                productType = "Rings",
                price = "1200",
                imageUrl = "https://example.com/ring.jpg",
            ),
            Product(
                id = "product-2",
                title = "Obsidian Chronograph",
                vendor = "Obsidian",
                productType = "Watches",
                price = "4500",
                imageUrl = "https://example.com/watch.jpg",
            ),
        ),
    )
}
