package com.iti.myapplication.ui.product.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iti.myapplication.ui.product.components.ImagePagerIndicator
import com.iti.myapplication.ui.product.intent.ProductDetailIntent
import com.iti.myapplication.ui.product.state.ProductDetailState
import iti.domain.product.model.ProductOption
import iti.domain.product.model.ProductVariant

/**
 * Stateless Compose screen for Product Detail.
 * Receives [state] and emits user events via [onIntent].
 * Contains zero business logic.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
    state: ProductDetailState,
    onIntent: (ProductDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.product?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> LoadingContent()
                state.error != null -> ErrorContent(
                    message = state.error,
                    onRetry = { onIntent(ProductDetailIntent.Retry) },
                )
                state.product != null -> ProductContent(
                    state = state,
                    onIntent = onIntent,
                )
            }
        }
    }
}

// region — Loading & Error

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
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

// endregion

// region — Product Content

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductContent(
    state: ProductDetailState,
    onIntent: (ProductDetailIntent) -> Unit,
) {
    val product = state.product ?: return
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        // ── Image Gallery ──────────────────────────────────────
        if (product.images.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { product.images.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) { page ->
                AsyncImage(
                    model = product.images[page].src,
                    contentDescription = product.images[page].alt ?: product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            ImagePagerIndicator(
                pageCount = product.images.size,
                currentPage = pagerState.currentPage,
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            // ── Vendor ─────────────────────────────────────────
            if (product.vendor.isNotBlank()) {
                Text(
                    text = product.vendor.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ── Title ──────────────────────────────────────────
            Text(
                text = product.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Price ──────────────────────────────────────────
            PriceRow(state = state)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Size / Option Selector ─────────────────────────
            product.options.forEach { option ->
                if (option.values.size > 1) {
                    OptionSelector(
                        option = option,
                        variants = product.variants,
                        selectedVariantId = state.selectedVariantId,
                        onSelectVariant = { variantId ->
                            onIntent(ProductDetailIntent.SelectVariant(variantId))
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // ── Size Required Error ────────────────────────────
            if (state.showSizeRequiredError) {
                Text(
                    text = "Please select a size before adding to cart",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Add to Cart Button ─────────────────────────────
            val selectedVariant = state.selectedVariantId?.let { id ->
                product.variants.find { it.id == id }
            }
            val isOutOfStock = selectedVariant?.inventoryQuantity == 0

            Button(
                onClick = { onIntent(ProductDetailIntent.AddToCart) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isOutOfStock,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                if (!isOutOfStock) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (isOutOfStock) "Out of stock" else "Add to Cart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Description ────────────────────────────────────
            if (product.description.isNotBlank()) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// endregion

// region — Price

@Composable
private fun PriceRow(state: ProductDetailState) {
    val product = state.product ?: return

    // Show selected variant's price if available, else the default product price
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

// endregion

// region — Option Selector (Size / Color chips)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionSelector(
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

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        option.values.forEach { value ->
            // Find the variant that matches this option value
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

// endregion
