package shopzen.presentation.product.screen

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import shopzen.domain.product.model.Product
import shopzen.domain.product.model.ProductVariant
import shopzen.presentation.R
import shopzen.presentation.cart.intent.CartIntent
import shopzen.presentation.cart.viewmodel.CartEffect
import shopzen.presentation.cart.viewmodel.CartViewModel
import shopzen.presentation.product.components.ErrorContent
import shopzen.presentation.product.components.ImagePagerIndicator
import shopzen.presentation.product.components.LoadingContent
import shopzen.presentation.product.components.OptionSelector
import shopzen.presentation.product.components.PriceRow
import shopzen.presentation.product.effect.ProductDetailEffect
import shopzen.presentation.product.intent.ProductDetailIntent
import shopzen.presentation.product.state.ProductDetailState
import shopzen.presentation.product.state.ProductReviewUi
import shopzen.presentation.product.viewmodel.ProductDetailViewModel

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
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(cartViewModel) {
        cartViewModel.effects.collect { effect ->
            when (effect) {
                CartEffect.NavigateToLogin -> onNavigateToLogin()
                is CartEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
                else -> Unit
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProductDetailEffect.NavigateToLogin -> onNavigateToLogin()
                is ProductDetailEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
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
                        text = state.product?.title ?: stringResource(R.string.product_detail_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
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
                    message = state.error.orEmpty(),
                    onRetry = { viewModel.processIntent(ProductDetailIntent.Retry) },
                )

                state.product != null -> ProductContent(
                    state = state,
                    onProductIntent = viewModel::processIntent,
                    onCartIntent = cartViewModel::processIntent,
                )
            }
        }
    }
}

@Composable
internal fun ProductContent(
    state: ProductDetailState,
    onProductIntent: (ProductDetailIntent) -> Unit,
    onCartIntent: (CartIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val product = state.product ?: return
    val selectedVariant = state.selectedVariant()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(ProductDetailTestTags.Content),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                ProductGallerySection(product = product)
            }
            item {
                ProductSummarySection(
                    state = state,
                    onWishlistClick = { onProductIntent(ProductDetailIntent.ToggleWishlist) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                ProductOptionsSection(
                    state = state,
                    onSelectVariant = { onProductIntent(ProductDetailIntent.SelectVariant(it)) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                ProductDescriptionSection(
                    description = product.description,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item {
                ProductReviewsSection(
                    isLoading = state.isReviewsLoading,
                    reviews = state.reviews,
                    error = state.reviewsError,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        ProductStickyActionBar(
            state = state,
            selectedVariant = selectedVariant,
            onAddToCart = { state.requestAddToCart(onProductIntent, onCartIntent) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ProductGallerySection(
    product: Product,
    modifier: Modifier = Modifier,
) {
    if (product.images.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag(ProductDetailTestTags.Gallery),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.product_detail_no_image),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { product.images.size })

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag(ProductDetailTestTags.Gallery),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.88f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) { page ->
            AsyncImage(
                model = product.images[page].src,
                contentDescription = product.images[page].alt ?: product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.scrim.copy(alpha = 0.12f),
                        0.7f to MaterialTheme.colorScheme.scrim.copy(alpha = 0f),
                        1f to MaterialTheme.colorScheme.scrim.copy(alpha = 0.22f),
                    ),
                ),
        )
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Text(
                text = stringResource(R.string.product_detail_image_count, pagerState.currentPage + 1, product.images.size),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }

    ImagePagerIndicator(
        pageCount = product.images.size,
        currentPage = pagerState.currentPage,
    )
}

@Composable
private fun ProductSummarySection(
    state: ProductDetailState,
    onWishlistClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val product = state.product ?: return
    val heartAlpha by animateFloatAsState(
        targetValue = if (state.isWishlistUpdating) 0.45f else 1f,
        animationSpec = tween(durationMillis = 160),
        label = "wishlist-heart-alpha",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(ProductDetailTestTags.Summary),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (product.vendor.isNotBlank()) {
                        Text(
                            text = product.vendor.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = product.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(heartAlpha)
                        .clickable(enabled = !state.isWishlistUpdating, onClick = onWishlistClick)
                        .testTag(ProductDetailTestTags.WishlistButton),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (state.isWishlisted) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (state.isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.product_detail_wishlist_cd),
                        )
                    }
                }
            }
            PriceRow(state = state)
            ReviewSnapshot(reviews = state.reviews)
        }
    }
}

@Composable
private fun ReviewSnapshot(
    reviews: List<ProductReviewUi>,
    modifier: Modifier = Modifier,
) {
    val average = reviews.averageRating()

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = if (reviews.isEmpty()) {
                stringResource(R.string.product_detail_no_reviews)
            } else {
                pluralStringResource(
                    R.plurals.product_detail_review_snapshot,
                    reviews.size,
                    average,
                    reviews.size,
                )
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProductOptionsSection(
    state: ProductDetailState,
    onSelectVariant: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val product = state.product ?: return

    if (product.options.none { it.values.size > 1 }) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(ProductDetailTestTags.Options),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeader(
                title = stringResource(R.string.product_detail_variants_title),
                subtitle = stringResource(R.string.product_detail_variants_subtitle),
            )
            product.options.forEach { option ->
                if (option.values.size > 1) {
                    OptionSelector(
                        option = option,
                        variants = product.variants,
                        selectedVariantId = state.selectedVariantId,
                        onSelectVariant = onSelectVariant,
                    )
                }
            }
            AnimatedVisibility(
                visible = state.showSizeRequiredError,
                enter = fadeIn(tween(140)) + slideInVertically { it / 3 },
                exit = fadeOut(tween(100)) + slideOutVertically { it / 3 },
            ) {
                Text(
                    text = stringResource(R.string.product_detail_size_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag(ProductDetailTestTags.SizeError),
                )
            }
        }
    }
}

@Composable
private fun ProductDescriptionSection(
    description: String,
    modifier: Modifier = Modifier,
) {
    if (description.isBlank()) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(ProductDetailTestTags.Description),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SectionHeader(
                title = stringResource(R.string.product_detail_description_title),
                subtitle = stringResource(R.string.product_detail_description_subtitle),
            )
            Text(
                text = description.stripHtml(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProductReviewsSection(
    isLoading: Boolean,
    reviews: List<ProductReviewUi>,
    error: String?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(ProductDetailTestTags.Reviews),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionHeader(
                title = stringResource(R.string.product_detail_reviews_title),
                subtitle = stringResource(R.string.product_detail_reviews_subtitle),
            )
            AnimatedContent(
                targetState = when {
                    isLoading -> "loading"
                    error != null -> "error"
                    reviews.isEmpty() -> "empty"
                    else -> "content"
                },
                transitionSpec = { fadeIn(tween(160)).togetherWith(fadeOut(tween(100))) },
                label = "product-reviews-state",
            ) { mode ->
                when (mode) {
                    "loading" -> Text(
                        text = stringResource(R.string.common_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    "error" -> Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )

                    "empty" -> Text(
                        text = stringResource(R.string.product_detail_reviews_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        reviews.take(3).forEach { review ->
                            ReviewRow(review = review)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(
    review: ProductReviewUi,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = review.authorName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
            Text(
                text = review.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProductStickyActionBar(
    state: ProductDetailState,
    selectedVariant: ProductVariant?,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val product = state.product ?: return
    val canAttemptAdd = state.canAttemptAddToCart()
    val outOfStock = !canAttemptAdd && product.hasConcreteVariantSelection(state.selectedVariantId)
    val displayPrice = selectedVariant?.price ?: product.price

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(ProductDetailTestTags.StickyCta),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.product_detail_total_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$$displayPrice",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Button(
                onClick = onAddToCart,
                enabled = canAttemptAdd || product.requiresVariantSelection(state.selectedVariantId),
                modifier = Modifier
                    .height(56.dp)
                    .width(178.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                if (!outOfStock) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (outOfStock) {
                        stringResource(R.string.product_detail_out_of_stock)
                    } else {
                        stringResource(R.string.product_detail_add_to_cart)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
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

private fun ProductDetailState.requestAddToCart(
    onProductIntent: (ProductDetailIntent) -> Unit,
    onCartIntent: (CartIntent) -> Unit,
) {
    val product = product ?: return
    if (product.requiresVariantSelection(selectedVariantId)) {
        onProductIntent(ProductDetailIntent.RequireVariantSelection)
        return
    }

    val variant = selectedVariant() ?: product.variants.firstOrNull() ?: return
    if (variant.inventoryQuantity <= 0) return

    onCartIntent(
        CartIntent.AddToCart(
            productId = product.id.toString(),
            variantId = variant.adminGraphqlApiId,
            title = product.title,
            variantTitle = variant.title,
            price = variant.price.toDoubleOrNull() ?: 0.0,
            maxQuantity = variant.inventoryQuantity,
            imageUrl = product.images.firstOrNull()?.src.orEmpty(),
        ),
    )
}

private fun ProductDetailState.selectedVariant(): ProductVariant? =
    selectedVariantId?.let { variantId ->
        product?.variants?.firstOrNull { it.id == variantId }
    }

private fun ProductDetailState.canAttemptAddToCart(): Boolean {
    val product = product ?: return false
    val selectedVariant = selectedVariant()
    return when {
        selectedVariant != null -> selectedVariant.inventoryQuantity > 0
        product.requiresVariantSelection(selectedVariantId) -> product.variants.any { it.inventoryQuantity > 0 }
        else -> product.variants.firstOrNull()?.inventoryQuantity?.let { it > 0 } == true
    }
}

private fun Product.requiresVariantSelection(selectedVariantId: Long?): Boolean =
    variants.size > 1 && options.any { it.values.size > 1 } && selectedVariantId == null

private fun Product.hasConcreteVariantSelection(selectedVariantId: Long?): Boolean =
    selectedVariantId != null || variants.size <= 1

private fun List<ProductReviewUi>.averageRating(): String {
    if (isEmpty()) return "0.0"
    return "%.1f".format(sumOf { it.rating }.toFloat() / size)
}

private fun String.stripHtml(): String =
    replace(Regex("<[^>]*>"), " ").replace(Regex("\\s+"), " ").trim()

internal object ProductDetailTestTags {
    const val Content = "product_detail_content"
    const val Gallery = "product_detail_gallery"
    const val Summary = "product_detail_summary"
    const val WishlistButton = "product_detail_wishlist_button"
    const val Options = "product_detail_options"
    const val SizeError = "product_detail_size_error"
    const val Description = "product_detail_description"
    const val Reviews = "product_detail_reviews"
    const val StickyCta = "product_detail_sticky_cta"
}
