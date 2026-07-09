package shopzen.presentation.category.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.category.intent.CategoryProductsIntent
import shopzen.presentation.category.viewmodel.CategoryProductsViewModel
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.components.ErrorScreen
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.common.components.ProductCard
import shopzen.presentation.wishlist.intent.WishlistIntent
import shopzen.presentation.wishlist.viewmodel.WishlistViewModel
import shopzen.presentation.comparison.viewmodel.ComparisonViewModel
import shopzen.presentation.comparison.screen.ComparisonTray
import shopzen.presentation.comparison.state.ComparisonIntent
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

/**
 * Displays products filtered by a specific category (Shopify product_type).
 * Includes full wishlist integration with confirmation dialogs,
 * matching the same pattern as HomeScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    categoryTitle: String,
    onNavigateBack: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToAiComparison: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryProductsViewModel = hiltViewModel(),
    wishlistViewModel: WishlistViewModel = hiltViewModel(),
    comparisonViewModel: ComparisonViewModel = hiltViewModel(androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val wishlistState by wishlistViewModel.state.collectAsStateWithLifecycle()
    val comparisonState by comparisonViewModel.state.collectAsStateWithLifecycle()

    val wishlistProductIds = wishlistState.items.map { it.productId }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = categoryTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
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
                    onRetry = { viewModel.processIntent(CategoryProductsIntent.Retry) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        items(state.products) { product ->
                            ProductCard(
                                id = product.id,
                                title = product.title,
                                category = product.productType,
                                price = product.price,
                                currency = state.currency,
                                imageUrl = product.imageUrl,
                                isFavorite = wishlistProductIds.contains(product.id),
                                onProductClick = onNavigateToProduct,
                                onFavoriteClick = {
                                    val isFav = wishlistProductIds.contains(product.id)
                                    if (isFav) {
                                        val item = wishlistState.items.find { it.productId == product.id }
                                        if (item != null) {
                                            wishlistViewModel.processIntent(
                                                WishlistIntent.RequestRemoveItem(item.id)
                                            )
                                        }
                                    } else {
                                        wishlistViewModel.processIntent(
                                            WishlistIntent.RequestAddToWishlist(product)
                                        )
                                    }
                                },
                                onCompareClick = {
                                    comparisonViewModel.processIntent(
                                        ComparisonIntent.AddProduct(
                                            shopzen.presentation.comparison.state.ComparableProductUiModel(
                                                productId = product.id,
                                                title = product.title,
                                                imageUrl = product.imageUrl,
                                                price = product.price.toDoubleOrNull() ?: 0.0,
                                                currency = state.currency.symbol
                                            )
                                        )
                                    )
                                }
                            )
                        }
                    }
                    ComparisonTray(
                        state = comparisonState,
                        onIntent = comparisonViewModel::processIntent,
                        onNavigateToComparison = onNavigateToAiComparison,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = innerPadding.calculateBottomPadding())
                    )
                }
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
                    WishlistIntent.DismissConfirmDialog
                )
            }
        )
    }
}
