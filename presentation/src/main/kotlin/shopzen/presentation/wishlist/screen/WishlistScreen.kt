package shopzen.presentation.wishlist.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import shopzen.domain.wishlist.model.WishlistItem
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.components.EmptyStateView
import shopzen.presentation.common.components.ErrorScreen
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.common.components.MainShellTab
import shopzen.presentation.common.components.ShopzenBottomBar
import shopzen.presentation.common.components.ShopzenTopAppBar
import shopzen.presentation.wishlist.WishlistTestTags
import shopzen.presentation.common.components.ProductCard
import shopzen.presentation.wishlist.intent.WishlistIntent
import shopzen.presentation.wishlist.state.WishlistState
import shopzen.presentation.wishlist.viewmodel.WishlistViewModel
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun WishlistScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::processIntent

    Scaffold(
        topBar = {
            ShopzenTopAppBar(
                onProfileClick = onNavigateToProfile,
                onCartClick = onNavigateToCart,
            )
        },
        bottomBar = {
            ShopzenBottomBar(
                currentTab = MainShellTab.WISHLIST,
                onDiscoverClick = onNavigateToHome,
                onSearchClick = onNavigateToSearch,
                onWishlistClick = {},
                onSettingsClick = onNavigateToSettings,
            )
        },
        modifier = modifier.background(MaterialTheme.colorScheme.background)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    ErrorScreen(
                        message = state.error.orEmpty(),
                        onRetry = { onIntent(WishlistIntent.LoadWishlist) }
                    )
                }
                else -> {
                    WishlistContent(
                        state = state,
                        onIntent = onIntent,
                        onNavigateToProduct = onNavigateToProduct,
                        onNavigateToHome = onNavigateToHome
                    )
                }
            }

            // Confirmation Dialog for removal
            val pendingRemovalId = state.pendingRemovalItemId
            if (state.showRemoveItemDialog && pendingRemovalId != null) {
                ConfirmationDialog(
                    title = "Remove Item",
                    message = "Remove this item from your wishlist?",
                    confirmText = "Remove",
                    dismissText = "Cancel",
                    onConfirm = {
                        onIntent(WishlistIntent.ConfirmRemoveItem(pendingRemovalId))
                    },
                    onDismiss = {
                        onIntent(WishlistIntent.DismissConfirmDialog)
                    }
                )
            }
        }
    }
}


@Composable
fun WishlistContent(
    state: WishlistState,
    onIntent: (WishlistIntent) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wishlist",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "${state.items.size} ITEMS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.items.isEmpty()) {
            EmptyStateView(
                message = "Your wishlist is empty",
                actionLabel = "Shop Now",
                onAction = onNavigateToHome
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(state.items, key = { it.id }) { item ->
                    ProductCard(
                        id = item.productId,
                        title = item.title,
                        category = item.vendor, // WishlistItem doesn't have productType, vendor is the closest
                        price = item.price,
                        currency = state.currency,
                        imageUrl = item.imageUrl,
                        isFavorite = true, // It's in the wishlist
                        onProductClick = onNavigateToProduct,
                        onFavoriteClick = {
                            onIntent(WishlistIntent.RequestRemoveItem(item.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WishlistAnimatedRow(
    rowItems: List<WishlistItem>,
    onItemClick: (String) -> Unit,
    onRemoveClick: (String) -> Unit,
    onAddToCartClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(rowItems.firstOrNull()?.id) {
        delay(60L.milliseconds)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 10 },
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            rowItems.forEach { item ->
                ProductCard(
                    id = item.productId,
                    title = item.title,
                    category = item.vendor,
                    price = item.price,
                    currency = shopzen.domain.profile.model.AppCurrency.USD, // Animated row not actually used, placeholder
                    imageUrl = item.imageUrl,
                    isFavorite = true,
                    onProductClick = onItemClick,
                    onFavoriteClick = { onRemoveClick(item.id) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag(WishlistTestTags.item(item.id)),
                )
            }
            if (rowItems.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
