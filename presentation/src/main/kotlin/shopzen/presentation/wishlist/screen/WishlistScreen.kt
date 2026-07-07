package shopzen.presentation.wishlist.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import shopzen.domain.wishlist.model.WishlistItem
import shopzen.presentation.R
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.components.ErrorScreen
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.common.components.MainShellTab
import shopzen.presentation.common.components.ShopzenBottomBar
import shopzen.presentation.common.components.ShopzenTopAppBar
import shopzen.presentation.wishlist.WishlistTestTags
import shopzen.presentation.wishlist.components.WishlistItemCard
import shopzen.presentation.wishlist.intent.WishlistIntent
import shopzen.presentation.wishlist.state.WishlistState
import shopzen.presentation.wishlist.viewmodel.WishlistViewModel

@Composable
fun WishlistScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WishlistViewModel = hiltViewModel(),
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
        modifier = modifier.background(MaterialTheme.colorScheme.background),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            when {
                state.isLoading && state.items.isEmpty() -> {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    ErrorScreen(
                        message = state.error.orEmpty(),
                        onRetry = { onIntent(WishlistIntent.LoadWishlist) },
                    )
                }

                else -> {
                    WishlistContent(
                        state = state,
                        onIntent = onIntent,
                        onNavigateToProduct = onNavigateToProduct,
                        onNavigateToHome = onNavigateToHome,
                    )
                }
            }

            val pendingRemovalId = state.pendingRemovalItemId
            if (state.showRemoveItemDialog && pendingRemovalId != null) {
                ConfirmationDialog(
                    title = stringResource(R.string.wishlist_remove_title),
                    message = stringResource(R.string.wishlist_remove_message),
                    confirmText = stringResource(R.string.wishlist_remove_confirm),
                    dismissText = stringResource(R.string.common_cancel),
                    onConfirm = { onIntent(WishlistIntent.ConfirmRemoveItem(pendingRemovalId)) },
                    onDismiss = { onIntent(WishlistIntent.DismissConfirmDialog) },
                )
            }
        }
    }
}

@Composable
internal fun WishlistContent(
    state: WishlistState,
    onIntent: (WishlistIntent) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(WishlistTestTags.Content),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            WishlistHeader(
                itemCount = state.items.size,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .testTag(WishlistTestTags.Header),
            )
        }

        if (state.items.isEmpty()) {
            item {
                WishlistEmptyState(
                    onShopNowClick = onNavigateToHome,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag(WishlistTestTags.Empty),
                )
            }
        } else {
            items(
                items = state.items.chunked(2),
                key = { rowItems -> rowItems.joinToString(separator = "_") { it.id } },
            ) { rowItems ->
                WishlistAnimatedRow(
                    rowItems = rowItems,
                    onItemClick = onNavigateToProduct,
                    onRemoveClick = { itemId -> onIntent(WishlistIntent.RequestRemoveItem(itemId)) },
                    onAddToCartClick = { productId -> onIntent(WishlistIntent.AddToCart(productId)) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun WishlistHeader(
    itemCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.wishlist_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.wishlist_subtitle),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = pluralStringResource(R.plurals.wishlist_item_count, itemCount, itemCount),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun WishlistEmptyState(
    onShopNowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.wishlist_empty_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.wishlist_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onShopNowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.wishlist_shop_now),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
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
        delay(60L)
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
                WishlistItemCard(
                    item = item,
                    onItemClick = onItemClick,
                    onRemoveClick = onRemoveClick,
                    onAddToCartClick = onAddToCartClick,
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
