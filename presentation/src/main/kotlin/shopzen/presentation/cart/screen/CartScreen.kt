package shopzen.presentation.cart.screen

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.R
import shopzen.presentation.cart.screen.content.CartEmptyState
import shopzen.presentation.cart.screen.content.CartErrorContent
import shopzen.presentation.cart.screen.content.CartListContent
import shopzen.presentation.cart.screen.content.CartLoadingContent
import shopzen.presentation.cart.components.CartTopBar
import shopzen.presentation.cart.components.CouponBottomSheet
import shopzen.presentation.cart.intent.CartIntent
import shopzen.presentation.cart.state.CartItemUi
import shopzen.presentation.cart.state.CartState
import shopzen.presentation.cart.state.isEmpty
import shopzen.presentation.cart.viewmodel.CartEffect
import shopzen.presentation.cart.viewmodel.CartViewModel
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.theme.LocalShopzenColors
import shopzen.presentation.common.theme.ShopzenMotion
import shopzen.presentation.common.theme.ShopzenTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    // Scoped to Activity so CartViewModel survives navigation and is shared across screens
    // (e.g. Cart, Checkout, bottom-nav badge).
    viewModel: CartViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CartEffect.NavigateToCheckout -> onNavigateToCheckout()
                is CartEffect.NavigateToProduct -> onNavigateToProduct(effect.productId)
                is CartEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
                CartEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    CartScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreenContent(
    modifier: Modifier = Modifier,
    state: CartState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onIntent: (CartIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val c = LocalShopzenColors.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(c.backgroundSecondary)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CartTopBar(
                hasItems = !state.isEmpty,
                onBack = onNavigateBack,
                onClearCart = { onIntent(CartIntent.RequestClearCart) },
            )

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
                        message = s.error.asString(),
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 80.dp),
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    CouponBottomSheet(
        visible = state.showCouponSheet,
        couponCode = state.couponCode,
        couponError = state.couponError?.asString(),
        isCouponLoading = state.isCouponLoading,
        sheetState = sheetState,
        onCodeChange = { onIntent(CartIntent.UpdateCouponCode(it)) },
        onApply = { onIntent(CartIntent.ApplyCoupon) },
        onDismiss = { onIntent(CartIntent.DismissCouponSheet) },
    )

    if (state.showRemoveItemDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.cart_remove_item_title),
            message = stringResource(R.string.cart_remove_item_msg),
            confirmText = stringResource(R.string.cart_remove),
            dismissText = stringResource(R.string.cart_cancel),
            onConfirm = { onIntent(CartIntent.ConfirmRemoveItem) },
            onDismiss = { onIntent(CartIntent.DismissRemoveItemDialog) },
        )
    }

    if (state.showClearCartDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.cart_clear_cart_title),
            message = stringResource(R.string.cart_clear_cart_msg),
            confirmText = stringResource(R.string.cart_clear_all),
            dismissText = stringResource(R.string.cart_cancel),
            onConfirm = { onIntent(CartIntent.ConfirmClearCart) },
            onDismiss = { onIntent(CartIntent.DismissClearCartDialog) },
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Cart â€” Filled (Light)")
@Composable
private fun CartScreenContentFilledLightPreview() {
    ShopzenTheme(darkTheme = false) {
        CartScreenContent(
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
private fun CartScreenContentFilledDarkPreview() {
    ShopzenTheme(darkTheme = true) {
        CartScreenContent(
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
private fun CartScreenContentEmptyPreview() {
    ShopzenTheme(darkTheme = true) {
        CartScreenContent(
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
