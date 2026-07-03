package shopzen.presentation.product.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import shopzen.presentation.product.state.ProductDetailState
import shopzen.domain.product.model.ProductOption
import shopzen.domain.product.model.ProductVariant

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun PriceRow(state: ProductDetailState) {
    val product = state.product ?: return

    val selectedVariant = state.selectedVariantId?.let { id ->
        product.variants.find { it.id == id }
    }
    val displayPrice = selectedVariant?.price ?: product.price
    val displayCompareAt = selectedVariant?.compareAtPrice ?: product.compareAtPrice

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$$displayPrice",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        if (!displayCompareAt.isNullOrBlank() && displayCompareAt != displayPrice) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$$displayCompareAt",
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OptionSelector(
    option: ProductOption,
    variants: List<ProductVariant>,
    selectedVariantId: Long?,
    onSelectVariant: (Long) -> Unit,
) {
    Text(
        text = option.name,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp),
    )

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        option.values.forEach { value ->
            val matchingVariant = variants.find { variant ->
                variant.selectedOptions.any {
                    it.name == option.name && it.value == value
                }
            }

            val isOutOfStock = matchingVariant?.inventoryQuantity == 0
            val isSelected = matchingVariant?.id == selectedVariantId

            FilterChip(
                selected = isSelected,
                onClick = {
                    matchingVariant?.let { onSelectVariant(it.id) }
                },
                label = {
                    Text(
                        text = if (isOutOfStock) "$value (Out of stock)" else value,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                enabled = !isOutOfStock,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                ),
            )
        }
    }
}
