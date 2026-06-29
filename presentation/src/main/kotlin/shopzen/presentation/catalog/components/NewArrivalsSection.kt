package shopzen.presentation.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import shopzen.domain.catalog.model.Product
import shopzen.presentation.common.components.ProductCard

/**
 * Section displaying the best sellers (or new arrivals) in a beautiful, structured 2-column fashion grid.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewArrivalsSection(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onWishlistClick: (Product) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
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
                text = "BEST SELLERS",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
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
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            maxItemsInEachRow = 2
        ) {
            val cardWidth = 160.dp
            products.take(6).forEach { product ->
                ProductCard(
                    product = product,
                    onProductClick = { onProductClick(product.id) },
                    onWishlistClick = { _ -> onWishlistClick(product) },
                    modifier = Modifier.width(cardWidth)
                )
            }
        }
    }
}
