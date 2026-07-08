package shopzen.presentation.catalog.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import shopzen.domain.catalog.model.Brand
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.presentation.R
import shopzen.presentation.catalog.components.AdBannerSection
import shopzen.presentation.catalog.intent.HomeIntent
import shopzen.presentation.catalog.state.HomeState
import shopzen.presentation.catalog.viewmodel.HomeViewModel
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.components.ErrorScreen
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.common.components.MainShellTab
import shopzen.presentation.common.components.ProductCard
import shopzen.presentation.common.components.ShopzenBottomBar
import shopzen.presentation.common.components.ShopzenTopAppBar
import shopzen.presentation.wishlist.intent.WishlistIntent
import shopzen.presentation.wishlist.viewmodel.WishlistViewModel

@Composable
fun HomeScreen(
    onNavigateToBrand: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    wishlistViewModel: WishlistViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val wishlistState by wishlistViewModel.state.collectAsStateWithLifecycle()
    val wishlistProductIds = wishlistState.items.map { it.productId }.toSet()

    Scaffold(
        topBar = {
            ShopzenTopAppBar(
                onProfileClick = onNavigateToProfile,
                onCartClick = onNavigateToCart,
            )
        },
        bottomBar = {
            ShopzenBottomBar(
                currentTab = MainShellTab.DISCOVER,
                onDiscoverClick = {},
                onSearchClick = onNavigateToSearch,
                onWishlistClick = onNavigateToWishlist,
                onSettingsClick = onNavigateToSettings,
            )
        },
        modifier = modifier.background(MaterialTheme.colorScheme.background),
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingIndicator(
                modifier = Modifier.padding(innerPadding),
            )

            state.error != null -> ErrorScreen(
                message = state.error.orEmpty(),
                onRetry = { viewModel.processIntent(HomeIntent.LoadHomeData) },
                modifier = Modifier.padding(innerPadding),
            )

            else -> HomeContent(
                state = state,
                wishlistProductIds = wishlistProductIds,
                onNavigateToBrand = onNavigateToBrand,
                onNavigateToCategory = onNavigateToCategory,
                onNavigateToProduct = onNavigateToProduct,
                onWishlistClick = { product ->
                    val wishlistItem = wishlistState.items.find { it.productId == product.id }
                    if (wishlistItem != null) {
                        wishlistViewModel.processIntent(WishlistIntent.RequestRemoveItem(wishlistItem.id))
                    } else {
                        wishlistViewModel.processIntent(WishlistIntent.RequestAddToWishlist(product))
                    }
                },
                onNavigateToProducts = onNavigateToProducts,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    if (wishlistState.showLoginRequiredDialog) {
        ConfirmationDialog(
            title = "Login Required",
            message = "You need to log in to add items to your wishlist.",
            confirmText = "Log In",
            dismissText = "Cancel",
            onConfirm = {
                wishlistViewModel.processIntent(WishlistIntent.DismissLoginRequiredDialog)
                onNavigateToLogin()
            },
            onDismiss = {
                wishlistViewModel.processIntent(WishlistIntent.DismissLoginRequiredDialog)
            },
        )
    }

    val pendingRemovalId = wishlistState.pendingRemovalItemId
    if (wishlistState.showRemoveItemDialog && pendingRemovalId != null) {
        val pendingItem = wishlistState.items.find { it.id == pendingRemovalId }
        ConfirmationDialog(
            title = stringResource(R.string.home_wishlist_remove_title),
            message = stringResource(
                R.string.home_wishlist_remove_message,
                pendingItem?.title ?: stringResource(R.string.home_wishlist_remove_fallback),
            ),
            confirmText = stringResource(R.string.home_wishlist_remove_confirm),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                wishlistViewModel.processIntent(WishlistIntent.ConfirmRemoveItem(pendingRemovalId))
            },
            onDismiss = {
                wishlistViewModel.processIntent(WishlistIntent.DismissConfirmDialog)
            },
        )
    }

    val pendingProduct = wishlistState.pendingAddProduct
    if (wishlistState.showAddConfirmationDialog && pendingProduct != null) {
        ConfirmationDialog(
            title = stringResource(R.string.home_wishlist_add_title),
            message = stringResource(R.string.home_wishlist_add_message, pendingProduct.title),
            confirmText = stringResource(R.string.home_wishlist_add_confirm),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = {
                wishlistViewModel.processIntent(WishlistIntent.ConfirmAddToWishlist(pendingProduct))
            },
            onDismiss = {
                wishlistViewModel.processIntent(WishlistIntent.DismissAddConfirmDialog)
            },
        )
    }
}

@Composable
internal fun HomeContent(
    state: HomeState,
    wishlistProductIds: Set<String>,
    onNavigateToBrand: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onWishlistClick: (Product) -> Unit,
    onNavigateToProducts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .testTag(HomeTestTags.Content),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        HomeAnimatedSection(index = 0) {
            HomeHeroSection(
                bannerImages = state.bannerImages,
                onExploreClick = onNavigateToProducts,
                modifier = Modifier.testTag(HomeTestTags.Hero),
            )
        }

        HomeAnimatedSection(index = 1) {
            HomeBrandSection(
                brands = state.brands,
                onBrandClick = onNavigateToBrand,
                modifier = Modifier.testTag(HomeTestTags.Brands),
            )
        }

        HomeAnimatedSection(index = 2) {
            AdBannerSection(
                ads = state.ads,
                modifier = Modifier.testTag(HomeTestTags.Ads),
            )
        }

        HomeAnimatedSection(index = 3) {
            HomeCategorySection(
                categories = state.categories,
                onCategoryClick = onNavigateToCategory,
                modifier = Modifier.testTag(HomeTestTags.Categories),
            )
        }

        HomeAnimatedSection(index = 4) {
            HomeNewArrivalsSection(
                products = state.newArrivals,
                wishlistProductIds = wishlistProductIds,
                onProductClick = onNavigateToProduct,
                onWishlistClick = onWishlistClick,
                onViewAllClick = onNavigateToProducts,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag(HomeTestTags.NewArrivals),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HomeAnimatedSection(
    index: Int,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 70L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 220)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 260),
                initialOffsetY = { it / 8 },
            ),
    ) {
        content()
    }
}

@Composable
private fun HomeHeroSection(
    bannerImages: List<String>,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp),
    ) {
        AsyncImage(
            model = bannerImages.firstOrNull() ?: HOME_FALLBACK_BANNER,
            contentDescription = stringResource(R.string.home_hero_image_cd),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.22f),
                            Color.Black.copy(alpha = 0.82f),
                        ),
                        startY = 120f,
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.home_hero_title),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_hero_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.86f),
            )
            Button(
                onClick = onExploreClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ),
                shape = RoundedCornerShape(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_hero_cta),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun HomeBrandSection(
    brands: List<Brand>,
    onBrandClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (brands.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HomeSectionHeader(
            title = stringResource(R.string.home_brands_title),
            subtitle = stringResource(R.string.home_brands_subtitle),
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(brands, key = { it.name }) { brand ->
                BrandPill(
                    brand = brand,
                    onClick = { onBrandClick(brand.name) },
                )
            }
        }
    }
}

@Composable
private fun BrandPill(
    brand: Brand,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .width(152.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = brand.name.take(2).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = brand.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HomeCategorySection(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (categories.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HomeSectionHeader(
            title = stringResource(R.string.home_categories_title),
            subtitle = stringResource(R.string.home_categories_subtitle),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(categories, key = { it.id }) { category ->
                CategoryFeatureCard(
                    category = category,
                    onClick = { onCategoryClick(category.id) },
                )
            }
        }
    }
}

@Composable
private fun CategoryFeatureCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(144.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AsyncImage(
            model = category.imageUrl,
            contentDescription = category.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.82f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Text(
            text = category.title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HomeNewArrivalsSection(
    products: List<Product>,
    wishlistProductIds: Set<String>,
    onProductClick: (String) -> Unit,
    onWishlistClick: (Product) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (products.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            HomeSectionHeader(
                title = stringResource(R.string.home_new_arrivals_title),
                subtitle = stringResource(R.string.home_new_arrivals_subtitle),
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(
                onClick = onViewAllClick,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    text = stringResource(R.string.home_view_all),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            products.chunked(2).forEach { rowProducts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    rowProducts.forEach { product ->
                        ProductCard(
                            product = product,
                            onProductClick = { onProductClick(product.id) },
                            onWishlistClick = { onWishlistClick(product) },
                            isFavorite = wishlistProductIds.contains(product.id),
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowProducts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal object HomeTestTags {
    const val Content = "home_content"
    const val Hero = "home_hero"
    const val Brands = "home_brands"
    const val Ads = "home_ads"
    const val Categories = "home_categories"
    const val NewArrivals = "home_new_arrivals"
}

private const val HOME_FALLBACK_BANNER =
    "https://images.unsplash.com/photo-1619134778706-7015533a6150?q=80&w=1200"