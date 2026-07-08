package shopzen.presentation.checkout.screen

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.R
import shopzen.presentation.checkout.components.AddressChoiceCard
import shopzen.presentation.checkout.components.CheckoutItemRow
import shopzen.presentation.checkout.components.CheckoutSection
import shopzen.presentation.checkout.components.CheckoutTopBar
import shopzen.presentation.checkout.components.PriceBreakdown
import shopzen.presentation.checkout.intent.CheckoutIntent
import shopzen.presentation.checkout.state.CheckoutState
import shopzen.presentation.checkout.viewmodel.CheckoutEffect
import shopzen.presentation.checkout.viewmodel.CheckoutViewModel
import shopzen.presentation.common.theme.LocalShopzenColors
import shopzen.presentation.common.theme.ShopzenBody
import shopzen.presentation.common.theme.ShopzenHeading3
import shopzen.presentation.common.theme.ShopzenMotion
import shopzen.presentation.common.theme.ShopzenShapes
import shopzen.presentation.common.theme.ShopzenSize
import shopzen.presentation.common.theme.ShopzenSmall
import shopzen.presentation.common.theme.ShopzenSpacing

@Composable
fun CheckoutSummaryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAddAddress: () -> Unit,
    onNavigateToPayment: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CheckoutEffect.NavigateToLogin -> onNavigateToLogin()
                CheckoutEffect.NavigateToAddAddress -> onNavigateToAddAddress()
                CheckoutEffect.NavigateToPayment -> onNavigateToPayment()
                is CheckoutEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
                is CheckoutEffect.NavigateToOrderConfirmation -> Unit
                CheckoutEffect.NavigateHome -> Unit
            }
        }
    }

    CheckoutSummaryContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun CheckoutSummaryContent(
    state: CheckoutState,
    snackbarHostState: SnackbarHostState,
    onIntent: (CheckoutIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(c.backgroundSecondary)
            .statusBarsPadding(),
        containerColor = c.backgroundSecondary,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CheckoutTopBar(
                title = stringResource(R.string.checkout_title),
                onNavigateBack = onNavigateBack,
            )
        },
        bottomBar = {
            CheckoutSummaryBottomBar(
                enabled = !state.isLoading,
                onContinue = { onIntent(CheckoutIntent.ContinueToPayment) },
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = state,
            contentKey = { s ->
                when {
                    s.isLoading -> "loading"
                    s.error != null && s.items.isEmpty() -> "error"
                    s.items.isEmpty() -> "empty"
                    else -> "content"
                }
            },
            transitionSpec = {
                fadeIn(tween(ShopzenMotion.DurationNormal)) togetherWith
                    fadeOut(tween(ShopzenMotion.DurationNormal))
            },
            label = "checkoutSummaryContent",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { current ->
            when {
                current.isLoading -> CheckoutMessage(
                    message = stringResource(R.string.checkout_loading),
                )
                current.error != null && current.items.isEmpty() -> CheckoutMessage(
                    message = current.error.asString(),
                    actionLabel = stringResource(R.string.checkout_retry),
                    onAction = { onIntent(CheckoutIntent.Retry) },
                )
                current.items.isEmpty() -> CheckoutMessage(
                    message = stringResource(R.string.checkout_empty_cart),
                    actionLabel = stringResource(R.string.checkout_retry),
                    onAction = { onIntent(CheckoutIntent.Retry) },
                )
                else -> CheckoutSummaryBody(
                    state = current,
                    onIntent = onIntent,
                )
            }
        }
    }
}

@Composable
private fun CheckoutSummaryBody(
    state: CheckoutState,
    onIntent: (CheckoutIntent) -> Unit,
) {
    val c = LocalShopzenColors.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ShopzenSpacing.LG,
            end = ShopzenSpacing.LG,
            top = ShopzenSpacing.LG,
            bottom = ShopzenSpacing.XXXXL,
        ),
        verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.LG),
    ) {
        state.error?.let { error ->
            item {
                InlineCheckoutError(
                    message = error.asString(),
                    onDismiss = { onIntent(CheckoutIntent.DismissError) },
                )
            }
        }

        item {
            CheckoutSection(
                titleContent = {
                    Text(
                        text = stringResource(R.string.checkout_items_title),
                        style = ShopzenHeading3,
                        color = c.textPrimary,
                    )
                },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD)) {
                    state.items.forEach { item ->
                        CheckoutItemRow(item = item)
                    }
                }
            }
        }

        item {
            CheckoutSection(
                titleContent = {
                    Text(
                        text = stringResource(R.string.checkout_address_title),
                        style = ShopzenHeading3,
                        color = c.textPrimary,
                    )
                },
                trailingContent = {
                    TextButton(onClick = { onIntent(CheckoutIntent.AddAddressClicked) }) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                        )
                        Text(text = stringResource(R.string.checkout_add_address))
                    }
                },
            ) {
                AnimatedVisibility(visible = state.addresses.isEmpty()) {
                    Text(
                        text = stringResource(R.string.checkout_no_addresses),
                        style = ShopzenBody,
                        color = LocalShopzenColors.current.textSecondary,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD)) {
                    state.addresses.forEach { address ->
                        AddressChoiceCard(
                            address = address,
                            selected = address.id == state.selectedAddressId,
                            onClick = {
                                onIntent(CheckoutIntent.SelectShippingAddress(address.id))
                            },
                        )
                    }
                }
            }
        }

        item {
            CheckoutSection(
                titleContent = {
                    Text(
                        text = stringResource(R.string.checkout_price_title),
                        style = ShopzenHeading3,
                        color = c.textPrimary,
                    )
                },
            ) {
                PriceBreakdown(
                    formattedSubtotal = state.formattedSubtotal,
                    formattedDiscount = state.formattedDiscount,
                    formattedTotal = state.formattedTotal,
                    appliedCouponLabel = state.appliedCouponLabel,
                )
            }
        }
    }
}

@Composable
private fun CheckoutSummaryBottomBar(
    enabled: Boolean,
    onContinue: () -> Unit,
) {
    val c = LocalShopzenColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.backgroundPrimary)
            .navigationBarsPadding()
            .padding(ShopzenSpacing.LG),
    ) {
        Button(
            onClick = onContinue,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(ShopzenSize.ButtonHeight),
            shape = ShopzenShapes.MD,
            colors = ButtonDefaults.buttonColors(
                containerColor = c.actionPrimaryBg,
                contentColor = c.actionPrimaryFg,
                disabledContainerColor = c.actionDisabledBg,
                disabledContentColor = c.actionDisabledFg,
            ),
        ) {
            Text(
                text = stringResource(R.string.checkout_continue_to_payment),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun InlineCheckoutError(
    message: String,
    onDismiss: () -> Unit,
) {
    val c = LocalShopzenColors.current
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        color = c.actionDangerBg,
        shape = ShopzenShapes.MD,
    ) {
        Column(Modifier.padding(ShopzenSpacing.MD)) {
            Text(
                text = message,
                style = ShopzenSmall,
                color = c.actionDangerFg,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(ShopzenSpacing.XS))
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.checkout_dismiss_error))
            }
        }
    }
}

@Composable
private fun CheckoutMessage(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = LocalShopzenColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ShopzenSpacing.XL),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = ShopzenBody,
            color = c.textSecondary,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(ShopzenSpacing.LG))
            Button(
                onClick = onAction,
                shape = ShopzenShapes.MD,
            ) {
                Text(text = actionLabel)
            }
        }
    }
}
