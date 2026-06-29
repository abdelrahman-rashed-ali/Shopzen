package shopzen.presentation.cart.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.cart.components.CartEmptyState
import shopzen.presentation.cart.components.CartItemCard
import shopzen.presentation.cart.components.CartSummarySection
import shopzen.presentation.cart.components.CouponBottomSheet
import shopzen.presentation.cart.intent.CartIntent
import shopzen.presentation.cart.state.CartItemUi
import shopzen.presentation.cart.state.CartState
import shopzen.presentation.cart.state.isEmpty

import shopzen.presentation.cart.viewmodel.CartEffect
import shopzen.presentation.cart.viewmodel.CartViewModel
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenElevation
import shopzen.presentation.theme.ShopzenHeading3
import shopzen.presentation.theme.ShopzenMotion
import shopzen.presentation.theme.ShopzenSectionTitle
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSize
import shopzen.presentation.theme.ShopzenSpacing
import shopzen.presentation.theme.ShopzenTheme

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//  STATE-HOLDER OVERLOAD â€” wires ViewModel, effects, navigation
//  Per compose-state-holder-ui-split skill: collects state + effects here,
//  delegates all layout to the pure UI overload below.
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect one-shot effects
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CartEffect.NavigateToCheckout -> onNavigateToCheckout()
                is CartEffect.NavigateToProduct -> onNavigateToProduct(effect.productId)
                is CartEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    CartScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
    )
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//  UI OVERLOAD â€” pure stateless renderer; previewable without DI
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    state: CartState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (CartIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    val couponSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.backgroundSecondary)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // â”€â”€ Top App Bar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            CartTopBar(
                itemCount = state.items.size,
                hasItems = !state.isEmpty,
                onBack = onNavigateBack,
                onClearCart = { onIntent(CartIntent.RequestClearCart) },
            )

            // â”€â”€ Main content â€” AnimatedContent switches loading/empty/list/error
            AnimatedContent(
                targetState = state,
                contentKey = { s ->
                    when {
                        s.isLoading -> "loading"
                        s.error != null -> "error"
                        s.isEmpty -> "empty"
                        else -> "content"
                    }
                },
                transitionSpec = {
                    (fadeIn(tween(ShopzenMotion.DurationNormal)) togetherWith
                        fadeOut(tween(ShopzenMotion.DurationNormal)))
                },
                label = "cartContent",
                modifier = Modifier.weight(1f),
            ) { s ->
                when {
                    s.isLoading -> CartLoadingContent()
                    s.error != null -> CartErrorContent(
                        message = s.error,
                        onRetry = { onIntent(CartIntent.Retry) },
                    )
                    s.isEmpty -> CartEmptyState(
                        onStartShopping = onNavigateBack,
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> CartListContent(
                        state = s,
                        onIntent = onIntent,
                    )
                }
            }
        }

        // â”€â”€ Snackbar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp),
        )
    }

    // â”€â”€ Coupon bottom sheet (outside Column so it overlays correctly) â”€â”€â”€â”€â”€â”€â”€â”€â”€
    CouponBottomSheet(
        visible = state.showCouponSheet,
        couponCode = state.couponCode,
        couponError = state.couponError,
        isCouponLoading = state.isCouponLoading,
        sheetState = couponSheetState,
        onCodeChange = { onIntent(CartIntent.UpdateCouponCode(it)) },
        onApply = { onIntent(CartIntent.ApplyCoupon) },
        onDismiss = { onIntent(CartIntent.DismissCouponSheet) },
    )

    // â”€â”€ Remove item confirmation dialog â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    ConfirmationDialog(
        visible = state.showRemoveItemDialog,
        title = "Remove Item",
        message = "Remove this item from your cart?",
        confirmText = "Remove",
        dismissText = "Cancel",
        onConfirm = { onIntent(CartIntent.ConfirmRemoveItem) },
        onDismiss = { onIntent(CartIntent.DismissRemoveItemDialog) },
    )

    // â”€â”€ Clear cart confirmation dialog â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    ConfirmationDialog(
        visible = state.showClearCartDialog,
        title = "Clear Cart",
        message = "Remove all items from your cart? This cannot be undone.",
        confirmText = "Clear All",
        dismissText = "Cancel",
        onConfirm = { onIntent(CartIntent.ConfirmClearCart) },
        onDismiss = { onIntent(CartIntent.DismissClearCartDialog) },
    )
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//  Sub-composables â€” private layout helpers
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun CartTopBar(
    itemCount: Int,
    hasItems: Boolean,
    onBack: () -> Unit,
    onClearCart: () -> Unit,
) {
    val c = LocalShopzenColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ShopzenSize.TopBarHeight)
            .background(c.surfaceNavigation)
            .padding(horizontal = ShopzenSpacing.XS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Back button
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = c.iconPrimary,
            )
        }

        // Title
        Text(
            text = "My Cart",
            style = ShopzenSectionTitle,
            color = c.textPrimary,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
        )

        // Clear all button â€” only visible when cart has items
        Box(
            modifier = Modifier.size(ShopzenSize.TopBarHeight),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = hasItems,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ) {
                IconButton(onClick = onClearCart) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "Clear cart",
                        tint = c.iconDestructive,
                    )
                }
            }
        }

    }
}

@Composable
private fun CartListContent(
    state: CartState,
    onIntent: (CartIntent) -> Unit,
) {
    val c = LocalShopzenColors.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Scrollable item list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = ShopzenSpacing.XL,
                vertical = ShopzenSpacing.LG,
            ),
            verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD),
        ) {
            items(
                items = state.items,
                key = { it.id },
            ) { item ->
                // AnimatedVisibility wraps each item for removal animation
                AnimatedVisibility(
                    visible = true,
                    exit = shrinkVertically() + fadeOut(tween(ShopzenMotion.DurationSlow)),
                ) {
                    CartItemCard(
                        item = item,
                        onIncrement = { onIntent(CartIntent.IncrementQuantity(item.id)) },
                        onDecrement = { onIntent(CartIntent.DecrementQuantity(item.id)) },
                        onDelete = { onIntent(CartIntent.RequestRemoveItem(item.id)) },
                        onCardClick = { onIntent(CartIntent.NavigateToProduct(item.productId)) },
                    )
                }
            }
        }

        // Summary + checkout â€” pinned to bottom
        Column(
            modifier = Modifier
                .background(c.backgroundPrimary)
                .navigationBarsPadding(),
        ) {
            CartSummarySection(
                formattedSubtotal = state.formattedSubtotal,
                formattedTotal = state.formattedTotal,
                formattedDiscount = state.formattedDiscount,
                couponApplied = state.couponApplied,
                appliedCouponLabel = state.appliedCouponLabel,
                onAddCoupon = { onIntent(CartIntent.OpenCouponSheet) },
                onRemoveCoupon = { onIntent(CartIntent.RemoveCoupon) },
                modifier = Modifier.padding(horizontal = ShopzenSpacing.XL),
            )

            CheckoutButton(
                onClick = { onIntent(CartIntent.Checkout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ShopzenSpacing.XL,
                        vertical = ShopzenSpacing.LG,
                    ),
            )
        }
    }
}

@Composable
private fun CheckoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Button(
        onClick = onClick,
        modifier = modifier
            .height(ShopzenSize.ButtonHeight)
            .shadow(
                elevation = ShopzenElevation.MD,
                shape = ShopzenShapes.Full,
                ambientColor = c.shadowMd,
                spotColor = c.shadowMd,
            )
            // Deferred scale read in graphicsLayer
            .graphicsLayer {
                val scale = if (isPressed) ShopzenMotion.ScaleButton else 1f
                scaleX = scale
                scaleY = scale
            },
        interactionSource = interactionSource,
        shape = ShopzenShapes.Full,
        colors = ButtonDefaults.buttonColors(
            containerColor = c.actionPrimaryBg,
            contentColor = c.actionPrimaryFg,
        ),
    ) {
        Text(
            text = "Checkout",
            style = ShopzenBody,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun CartLoadingContent() {
    val c = LocalShopzenColors.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = c.actionPrimaryBg,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
private fun CartErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    val c = LocalShopzenColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ShopzenSpacing.XL),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Something went wrong",
            style = ShopzenHeading3,
            color = c.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ShopzenSpacing.SM))
        Text(
            text = message,
            style = ShopzenBody,
            color = c.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ShopzenSpacing.XXL))
        Button(
            onClick = onRetry,
            shape = ShopzenShapes.Full,
            colors = ButtonDefaults.buttonColors(
                containerColor = c.actionPrimaryBg,
                contentColor = c.actionPrimaryFg,
            ),
        ) {
            Text(text = "Retry", style = ShopzenBody, fontWeight = FontWeight.SemiBold)
        }
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//  Previews
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Preview(showBackground = true, showSystemUi = true, name = "Cart â€” Filled (Light)")
@Composable
private fun CartScreenFilledLightPreview() {
    ShopzenTheme(darkTheme = false) {
        CartScreen(
            state = CartState(
                items = previewItems(),
                formattedSubtotal = "$245.00",
                formattedTotal = "$245.00",
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Cart â€” Filled (Dark)")
@Composable
private fun CartScreenFilledDarkPreview() {
    ShopzenTheme(darkTheme = true) {
        CartScreen(
            state = CartState(
                items = previewItems(),
                formattedSubtotal = "$245.00",
                formattedTotal = "$220.50",
                formattedDiscount = "-$24.50",
                couponApplied = true,
                appliedCouponLabel = "SAVE10 (-10%)",
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Cart â€” Empty")
@Composable
private fun CartScreenEmptyPreview() {
    ShopzenTheme {
        CartScreen(
            state = CartState(items = emptyList(), isLoading = false),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

private fun previewItems() = listOf(
    CartItemUi("1", "p1", "v1", "Woman Sweater", "Woman Fashion", "$70.00", 1, 10, ""),
    CartItemUi("2", "p2", "v2", "Smart Watch", "Electronics", "$55.00", 1, 5, ""),
    CartItemUi("3", "p3", "v3", "Wireless Headphone", "Electronics", "$120.00", 1, 8, ""),
)
