package shopzen.presentation.search.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.domain.search.model.SortOption
import shopzen.presentation.search.intent.SearchIntent
import shopzen.presentation.search.state.SearchState
import shopzen.presentation.theme.ShopzenTheme

class SearchScreenContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun discoveryModeRendersSuggestionsAndCollections() {
        val intents = mutableListOf<SearchIntent>()
        var openedCategory: String? = null

        composeRule.setContent {
            ShopzenTheme {
                SearchContent(
                    state = sampleSearchState(hasSearched = false),
                    onIntent = { intents += it },
                    onNavigateToProduct = {},
                    onNavigateToCategory = { openedCategory = it },
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(SearchTestTags.Discover).assertIsDisplayed()
        composeRule.onNodeWithText("Suggestions").assertIsDisplayed()
        composeRule.onNodeWithText("Explore Collections").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Gold Bracelets").performClick()
        composeRule.onNodeWithTag(SearchTestTags.FilterButton).performClick()
        composeRule.onNodeWithTag(SearchTestTags.collection("timepieces")).performScrollTo().performClick()

        assertEquals(
            listOf(
                SearchIntent.SelectSuggestion("Gold Bracelets"),
                SearchIntent.ToggleFilterSheet,
            ),
            intents,
        )
        assertEquals("timepieces", openedCategory)
    }

    @Test
    fun resultsModeRendersActiveFiltersAndWiresProductClick() {
        var openedProduct: String? = null

        composeRule.setContent {
            ShopzenTheme {
                SearchContent(
                    state = sampleSearchState(
                        query = "watch",
                        hasSearched = true,
                        selectedCategory = "timepieces",
                        selectedBrand = "Obsidian",
                        selectedSortOption = SortOption.PRICE_LOW_TO_HIGH,
                    ),
                    onIntent = {},
                    onNavigateToProduct = { openedProduct = it },
                    onNavigateToCategory = {},
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(SearchTestTags.Results).assertIsDisplayed()
        composeRule.onNodeWithTag(SearchTestTags.ActiveFilters).assertIsDisplayed()
        composeRule.onNodeWithText("2 results").assertIsDisplayed()
        composeRule.onNodeWithText("\"watch\"").assertIsDisplayed()
        composeRule.onNodeWithText("Timepieces").assertIsDisplayed()
        composeRule.onNodeWithTag(SearchTestTags.product("product-1")).performScrollTo().performClick()

        assertEquals("product-1", openedProduct)
    }

    @Test
    fun emptyResultsShowsClearAction() {
        val intents = mutableListOf<SearchIntent>()

        composeRule.setContent {
            ShopzenTheme {
                SearchContent(
                    state = sampleSearchState(
                        query = "missing",
                        hasSearched = true,
                        filteredProducts = emptyList(),
                    ),
                    onIntent = { intents += it },
                    onNavigateToProduct = {},
                    onNavigateToCategory = {},
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(SearchTestTags.Empty).assertIsDisplayed()
        composeRule.onNodeWithText("No results found").assertIsDisplayed()
        composeRule.onNodeWithText("Clear filters").performClick()

        assertEquals(listOf(SearchIntent.ClearFilters), intents)
    }

    private fun sampleSearchState(
        query: String = "",
        hasSearched: Boolean,
        selectedCategory: String? = null,
        selectedBrand: String? = null,
        selectedSortOption: SortOption = SortOption.DEFAULT,
        filteredProducts: List<Product> = sampleProducts(),
    ) = SearchState(
        query = query,
        hasSearched = hasSearched,
        allProducts = sampleProducts(),
        filteredProducts = filteredProducts,
        categories = sampleCategories(),
        suggestions = listOf("Gold Bracelets", "Swiss Watches", "Diamond Rings"),
        selectedCategory = selectedCategory,
        selectedBrand = selectedBrand,
        selectedSortOption = selectedSortOption,
    )

    private fun sampleCategories() = listOf(
        Category(
            id = "timepieces",
            title = "Timepieces",
            imageUrl = "https://example.com/timepieces.jpg",
        ),
        Category(
            id = "bracelets",
            title = "Bracelets",
            imageUrl = "https://example.com/bracelets.jpg",
        ),
        Category(
            id = "rings",
            title = "Rings",
            imageUrl = "https://example.com/rings.jpg",
        ),
    )

    private fun sampleProducts() = listOf(
        Product(
            id = "product-1",
            title = "Obsidian Chronograph",
            vendor = "Obsidian",
            productType = "Timepieces",
            price = "4500",
            imageUrl = "https://example.com/watch.jpg",
        ),
        Product(
            id = "product-2",
            title = "Aethelgard Bracelet",
            vendor = "Maison Lumi",
            productType = "Bracelets",
            price = "1200",
            imageUrl = "https://example.com/bracelet.jpg",
        ),
    )
}
