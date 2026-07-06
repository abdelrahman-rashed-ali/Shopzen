package shopzen.presentation.product.screen

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import shopzen.domain.cart.model.CartItem
import shopzen.presentation.cart.intent.CartIntent
import shopzen.presentation.cart.viewmodel.CartEffect
import shopzen.presentation.cart.viewmodel.CartViewModel
import shopzen.presentation.product.components.ErrorContent
import shopzen.presentation.product.components.ImagePagerIndicator
import shopzen.presentation.product.components.LoadingContent
import shopzen.presentation.product.components.OptionSelector
import shopzen.presentation.product.components.PriceRow
import shopzen.presentation.product.intent.ProductDetailIntent
import shopzen.presentation.product.state.ProductDetailState
import shopzen.presentation.product.viewmodel.ProductDetailViewModel

/**
 * Stateless Compose screen for Product Detail.
 * Contains zero business logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    productId: Long?,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::processIntent

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(cartViewModel) {
        cartViewModel.effects.collect { effect ->
            when (effect) {
                CartEffect.NavigateToLogin -> onNavigateToLogin()
                is CartEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
                else -> {}
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                    onProductIntent = onIntent,
                    onCartIntent = cartViewModel::processIntent
                )
            }
        }
    }
}

// region — Product Content

@Composable
fun ProductContent(
    state: ProductDetailState,
    onProductIntent: (ProductDetailIntent) -> Unit,
    onCartIntent: (CartIntent) -> Unit,
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
                            onProductIntent(ProductDetailIntent.SelectVariant(variantId))
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
                onClick = { onCartIntent(CartIntent.AddToCart(
                    productId = product.id.toString(),
                    variantId = selectedVariant?.adminGraphqlApiId ?: "",
                    title = product.title,
                    variantTitle = selectedVariant?.title ?: "",
                    price = selectedVariant?.price?.toDouble() ?: 0.0,
                    maxQuantity = selectedVariant?.inventoryQuantity ?: 1,
                    imageUrl = product.images.firstOrNull()?.src ?: ""
                )) },
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
