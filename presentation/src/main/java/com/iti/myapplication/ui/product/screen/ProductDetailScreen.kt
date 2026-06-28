package com.iti.myapplication.ui.product.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.iti.myapplication.ui.product.components.ErrorContent
import com.iti.myapplication.ui.product.components.ImagePagerIndicator
import com.iti.myapplication.ui.product.components.LoadingContent
import com.iti.myapplication.ui.product.components.OptionSelector
import com.iti.myapplication.ui.product.components.PriceRow
import com.iti.myapplication.ui.product.intent.ProductDetailIntent
import com.iti.myapplication.ui.product.state.ProductDetailState
import com.iti.myapplication.ui.product.viewmodel.ProductDetailViewModel

/**
 * Stateless Compose screen for Product Detail.
 * Contains zero business logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::processIntent

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
                    message = state.error!!,
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

// region — Product Content

@Composable
fun ProductContent(
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

