package shopzen.presentation.wishlist.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import shopzen.presentation.common.components.BottomTab
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.components.EmptyStateView
import shopzen.presentation.common.components.ErrorScreen
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.common.components.MainBottomBar
import shopzen.presentation.wishlist.components.WishlistItemCard
import shopzen.presentation.wishlist.intent.WishlistIntent
import shopzen.presentation.wishlist.state.WishlistState
import shopzen.presentation.wishlist.viewmodel.WishlistViewModel

@Composable
fun WishlistScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WishlistViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::processIntent

    Scaffold(
        topBar = { WishlistTopBar() },
        bottomBar = {
            MainBottomBar(
                currentTab = BottomTab.WISHLIST,
                onTabClick = { tab ->
                    if (tab == BottomTab.HOME) {
                        onNavigateToHome()
                    }
                }
            )
        },
        modifier = modifier.background(Color.White)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
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
                    message = "Remove this item from your wishlist?",
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
private fun WishlistTopBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Open drawer */ }) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }

            // Center LUMINA text logo matching screenshot
            Text(
                text = "LUMINA",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 4.sp
                ),
                color = Color.Black
            )

            IconButton(onClick = { /* View notifications */ }) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.Black
                )
            }
        }
        // Subtle divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color(0xFFEEEEEE))
        )
    }
}

@Composable
private fun WishlistContent(
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
                color = Color.Black
            )

            Text(
                text = "${state.items.size} ITEMS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                ),
                color = Color.Gray
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
                    WishlistItemCard(
                        item = item,
                        onItemClick = onNavigateToProduct,
                        onRemoveClick = { itemId ->
                            onIntent(WishlistIntent.RequestRemoveItem(itemId))
                        },
                        onAddToCartClick = { productId ->
                            onIntent(WishlistIntent.AddToCart(productId))
                        }
                    )
                }
            }
        }
    }
}
