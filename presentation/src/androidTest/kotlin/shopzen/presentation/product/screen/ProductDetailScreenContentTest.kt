package shopzen.presentation.product.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import shopzen.domain.product.model.Product
import shopzen.domain.product.model.ProductImage
import shopzen.domain.product.model.ProductOption
import shopzen.domain.product.model.ProductVariant
import shopzen.domain.product.model.SelectedOption
import shopzen.presentation.cart.intent.CartIntent
import shopzen.presentation.product.intent.ProductDetailIntent
import shopzen.presentation.product.state.ProductDetailState
import shopzen.presentation.product.state.ProductReviewUi
import shopzen.presentation.theme.ShopzenTheme

class ProductDetailScreenContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun productDetailRendersGallerySummaryOptionsDescriptionReviewsAndStickyCta() {
        composeRule.setContent {
            ShopzenTheme {
                ProductContent(
                    state = sampleState(),
                    onProductIntent = {},
                    onCartIntent = {},
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(ProductDetailTestTags.Gallery).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductDetailTestTags.Summary).assertIsDisplayed()
        composeRule.onNodeWithTag(ProductDetailTestTags.Options).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(ProductDetailTestTags.Description).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(ProductDetailTestTags.Reviews).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag(ProductDetailTestTags.StickyCta).assertIsDisplayed()
        composeRule.onNodeWithText("4.5 rating · 2 reviews").assertIsDisplayed()
    }

    @Test
    fun wishlistAndAddToCartActionsEmitExpectedEvents() {
        val productIntents = mutableListOf<ProductDetailIntent>()
        val cartIntents = mutableListOf<CartIntent>()

        composeRule.setContent {
            ShopzenTheme {
                ProductContent(
                    state = sampleState(),
                    onProductIntent = { productIntents += it },
                    onCartIntent = { cartIntents += it },
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithTag(ProductDetailTestTags.WishlistButton).performClick()
        composeRule.onNodeWithText("Add to Cart").performClick()

        assertEquals(listOf(ProductDetailIntent.ToggleWishlist), productIntents)
        assertTrue(cartIntents.single() is CartIntent.AddToCart)
        val addToCart = cartIntents.single() as CartIntent.AddToCart
        assertEquals("9001", addToCart.productId)
        assertEquals("gid://shopify/ProductVariant/1", addToCart.variantId)
        assertEquals("Signature Jacket", addToCart.title)
        assertEquals(3, addToCart.maxQuantity)
    }

    @Test
    fun addToCartWithoutVariantRequestsSelectionInsteadOfCartMutation() {
        val productIntents = mutableListOf<ProductDetailIntent>()
        val cartIntents = mutableListOf<CartIntent>()

        composeRule.setContent {
            ShopzenTheme {
                ProductContent(
                    state = sampleState(selectedVariantId = null),
                    onProductIntent = { productIntents += it },
                    onCartIntent = { cartIntents += it },
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Add to Cart").performClick()

        assertEquals(listOf(ProductDetailIntent.RequireVariantSelection), productIntents)
        assertTrue(cartIntents.isEmpty())
    }

    @Test
    fun selectedOutOfStockVariantDisablesStickyCta() {
        composeRule.setContent {
            ShopzenTheme {
                ProductContent(
                    state = sampleState(selectedVariantId = 2L),
                    onProductIntent = {},
                    onCartIntent = {},
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Out of stock").assertIsNotEnabled()
    }

    private fun sampleState(selectedVariantId: Long? = 1L) = ProductDetailState(
        isLoading = false,
        product = sampleProduct(),
        selectedVariantId = selectedVariantId,
        reviews = listOf(
            ProductReviewUi(
                id = "review-1",
                authorName = "Mona",
                rating = 5,
                body = "The tailoring is clean and the fabric feels premium.",
            ),
            ProductReviewUi(
                id = "review-2",
                authorName = "Omar",
                rating = 4,
                body = "Looks sharp and arrived in good condition.",
            ),
        ),
    )

    private fun sampleProduct() = Product(
        id = 9001L,
        title = "Signature Jacket",
        description = "<p>A structured jacket with clean lines.</p>",
        vendor = "Atelier North",
        productType = "Outerwear",
        tags = listOf("jacket", "premium"),
        images = listOf(
            ProductImage(
                id = 100L,
                src = "https://example.com/jacket.jpg",
                alt = "Signature Jacket",
            ),
        ),
        variants = listOf(
            ProductVariant(
                id = 1L,
                title = "Small",
                price = "120.00",
                compareAtPrice = "150.00",
                inventoryQuantity = 3,
                adminGraphqlApiId = "gid://shopify/ProductVariant/1",
                selectedOptions = listOf(SelectedOption(name = "Size", value = "S")),
            ),
            ProductVariant(
                id = 2L,
                title = "Medium",
                price = "120.00",
                compareAtPrice = "150.00",
                inventoryQuantity = 0,
                adminGraphqlApiId = "gid://shopify/ProductVariant/2",
                selectedOptions = listOf(SelectedOption(name = "Size", value = "M")),
            ),
        ),
        options = listOf(
            ProductOption(
                id = 10L,
                name = "Size",
                values = listOf("S", "M"),
            ),
        ),
        price = "120.00",
        compareAtPrice = "150.00",
    )
}
