package shopzen.presentation.brand.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.domain.catalog.model.Product
import shopzen.presentation.brand.intent.BrandProductsIntent
import shopzen.presentation.brand.viewmodel.BrandProductsViewModel
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.components.ErrorScreen
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.common.components.ProductCard
import shopzen.presentation.wishlist.intent.WishlistIntent
import shopzen.presentation.wishlist.viewmodel.WishlistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandProductsScreen(
    brandName: String,
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BrandProductsViewModel = hiltViewModel(),
    wishlistViewModel: WishlistViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val wishlistState by wishlistViewModel.state.collectAsStateWithLifecycle()

    val wishlistProductIds = wishlistState.items.map { it.productId }.toSet()

    LaunchedEffect(brandName) {
        viewModel.processIntent(BrandProductsIntent.LoadProducts(brandName))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = brandName.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.SansSerif
                        ),
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        modifier = modifier.background(Color.White)
    ) { innerPadding ->
        when {
            state.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(innerPadding))
            }
            state.error != null -> {
                ErrorScreen(
                    message = state.error.orEmpty(),
                    onRetry = { viewModel.processIntent(BrandProductsIntent.LoadProducts(brandName)) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {
                BrandProductsContent(
                    products = state.products,
                    currency = state.currency,
                    wishlistProductIds = wishlistProductIds,
                    onProductClick = onNavigateToProduct,
                    onWishlistClick = { product ->
                        val isFav = wishlistProductIds.contains(product.id)
                        if (isFav) {
                            val item = wishlistState.items.find { it.productId == product.id }
                            if (item != null) {
                                wishlistViewModel.processIntent(WishlistIntent.RequestRemoveItem(item.id))
                            }
                        } else {
                            wishlistViewModel.processIntent(WishlistIntent.RequestAddToWishlist(product))
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // Confirmation dialog for removing from wishlist
    if (wishlistState.showRemoveItemDialog && wishlistState.pendingRemovalItemId != null) {
        val pendingId = wishlistState.pendingRemovalItemId!!
        val pendingItem = wishlistState.items.find { it.id == pendingId }
        ConfirmationDialog(
            title = "Remove Item",
            message = "Remove ${pendingItem?.title ?: "this item"} from your wishlist?",
            confirmText = "Remove",
            dismissText = "Cancel",
            onConfirm = {
                wishlistViewModel.processIntent(
                    WishlistIntent.ConfirmRemoveItem(pendingId)
                )
            },
            onDismiss = {
                wishlistViewModel.processIntent(
                    WishlistIntent.DismissConfirmDialog
                )
            }
        )
    }

    // Confirmation dialog for adding to wishlist
    if (wishlistState.showAddConfirmationDialog && wishlistState.pendingAddProduct != null) {
        val pendingProduct = wishlistState.pendingAddProduct!!
        ConfirmationDialog(
            title = "Add to Wishlist",
            message = "Add ${pendingProduct.title} to your wishlist?",
            confirmText = "Add",
            dismissText = "Cancel",
            onConfirm = {
                wishlistViewModel.processIntent(
                    WishlistIntent.ConfirmAddToWishlist(pendingProduct)
                )
            },
            onDismiss = {
                wishlistViewModel.processIntent(
                    WishlistIntent.DismissAddConfirmDialog
                )
            }
        )
    }
}

@Composable
private fun BrandProductsContent(
    products: List<Product>,
    currency: shopzen.domain.profile.model.AppCurrency,
    wishlistProductIds: Set<String>,
    onProductClick: (String) -> Unit,
    onWishlistClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No products found for this brand",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products) { product ->
            ProductCard(
                id = product.id,
                title = product.title,
                category = product.productType,
                price = product.price,
                currency = currency,
                imageUrl = product.imageUrl,
                isFavorite = wishlistProductIds.contains(product.id),
                onProductClick = onProductClick,
                onFavoriteClick = { onWishlistClick(product) }
            )
        }
    }
}
