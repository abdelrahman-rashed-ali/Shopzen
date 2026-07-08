package shopzen.presentation.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.catalog.model.Product
import shopzen.presentation.common.components.ProductCard

/**
 * Section displaying the best sellers (or new arrivals) in a beautiful, structured 2-column fashion grid.
 */
@Composable
fun NewArrivalsSection(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onWishlistClick: (Product) -> Unit,
    onViewAllClick: () -> Unit,
    currency: AppCurrency,
    modifier: Modifier = Modifier,
    wishlistProductIds: Set<String> = emptySet()
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Best Sellers",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                ),
                color = Color.Black
            )

            // VIEW ALL link
            Text(
                text = "VIEW ALL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    textDecoration = TextDecoration.Underline
                ),
                color = Color.Black,
                modifier = Modifier.clickable(onClick = onViewAllClick)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2-Column Fashion Layout Grid
        val chunkedProducts = products.chunked(2)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            chunkedProducts.forEach { rowProducts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowProducts.forEach { product ->
                        ProductCard(
                            id = product.id,
                            title = product.title,
                            category = product.productType,
                            price = product.price,
                            currency = currency,
                            imageUrl = product.imageUrl,
                            isFavorite = wishlistProductIds.contains(product.id),
                            onProductClick = { onProductClick(product.id) },
                            onFavoriteClick = { onWishlistClick(product) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // If odd number of items, insert spacer to align layout
                    if (rowProducts.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
