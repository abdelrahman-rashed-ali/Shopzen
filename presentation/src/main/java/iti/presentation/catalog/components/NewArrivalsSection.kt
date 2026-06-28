package iti.presentation.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import iti.domain.catalog.model.Product
import iti.presentation.common.components.ProductCard

/**
 * New Arrivals section displaying products in a 2-column layout.
 * Uses the shared [ProductCard] composable — does NOT re-implement it.
 *
 * @param products List of new arrival products.
 * @param onProductClick Callback with product ID when a card is tapped.
 * @param onWishlistClick Callback with product ID when the heart is tapped.
 * @param onViewAllClick Callback when "VIEW ALL" is tapped.
 */
@Composable
fun NewArrivalsSection(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onWishlistClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) return

    // Section Header
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "New Arrivals",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "VIEW ALL",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clickable(onClick = onViewAllClick)
        )
    }

    // 2-column product grid (manual rows since we're inside a scrollable Column)
    val chunkedProducts = products.chunked(2)
    chunkedProducts.forEach { rowProducts ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            rowProducts.forEach { product ->
                ProductCard(
                    product = product,
                    onProductClick = onProductClick,
                    onWishlistClick = onWishlistClick,
                    modifier = Modifier.weight(1f)
                )
            }
            // Fill remaining space if odd number of products
            if (rowProducts.size == 1) {
                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}
