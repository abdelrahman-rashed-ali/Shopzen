package shopzen.presentation.profile.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.R
import shopzen.presentation.checkout.components.CheckoutTopBar
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.profile.components.OrderHistoryCard
import shopzen.presentation.profile.intent.OrderHistoryIntent
import shopzen.presentation.profile.state.OrderHistoryState
import shopzen.presentation.profile.viewmodel.OrderHistoryViewModel
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenHeading3
import shopzen.presentation.theme.ShopzenMotion
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSize
import shopzen.presentation.theme.ShopzenSpacing

@Composable
fun OrderHistoryScreen(
    onNavigateBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.processIntent(OrderHistoryIntent.LoadOrders)
    }

    OrderHistoryContent(
        state = state,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
        onOrderClick = onOrderClick,
        modifier = modifier,
    )
}

@Composable
fun OrderHistoryContent(
    state: OrderHistoryState,
    onIntent: (OrderHistoryIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onOrderClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(c.backgroundSecondary)
            .statusBarsPadding(),
        containerColor = c.backgroundSecondary,
        topBar = {
            CheckoutTopBar(
                title = stringResource(R.string.order_history_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = state,
            contentKey = { current ->
                when {
                    current.isLoading -> "loading"
                    current.error != null && current.orders.isEmpty() -> "error"
                    current.orders.isEmpty() -> "empty"
                    else -> "content"
                }
            },
            transitionSpec = {
                fadeIn(tween(ShopzenMotion.DurationNormal)) togetherWith
                    fadeOut(tween(ShopzenMotion.DurationNormal))
            },
            label = "orderHistoryContent",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { current ->
            when {
                current.isLoading -> LoadingIndicator()
                current.error != null && current.orders.isEmpty() -> OrderHistoryMessage(
                    title = current.error.asString(),
                    actionLabel = stringResource(R.string.order_history_retry),
                    onAction = { onIntent(OrderHistoryIntent.Retry) },
                )
                current.orders.isEmpty() -> OrderHistoryMessage(
                    title = stringResource(R.string.order_history_empty),
                    actionLabel = stringResource(R.string.order_history_retry),
                    onAction = { onIntent(OrderHistoryIntent.Retry) },
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = ShopzenSpacing.LG,
                        top = ShopzenSpacing.LG,
                        end = ShopzenSpacing.LG,
                        bottom = ShopzenSpacing.XXXL,
                    ),
                    verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.LG),
                ) {
                    items(current.orders, key = { it.id }) { order ->
                        val onOrderClickRemembered = remember(order.id, onOrderClick) {
                            { onOrderClick(order.id) }
                        }
                        OrderHistoryCard(
                            order = order,
                            onClick = onOrderClickRemembered,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderHistoryMessage(
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ShopzenSpacing.XL),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = ShopzenHeading3,
            color = c.textPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(ShopzenSpacing.LG))
        Button(
            onClick = onAction,
            modifier = Modifier.height(ShopzenSize.ButtonHeight),
            shape = ShopzenShapes.MD,
            colors = ButtonDefaults.buttonColors(
                containerColor = c.actionPrimaryBg,
                contentColor = c.actionPrimaryFg,
            ),
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
