package shopzen.presentation.checkout.screen

import android.util.Log
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paymob.paymob_sdk.ui.PaymobSdkListener
import shopzen.presentation.R
import shopzen.presentation.checkout.components.CheckoutSection
import shopzen.presentation.checkout.components.CheckoutTopBar
import shopzen.presentation.checkout.components.PaymentMethodRow
import shopzen.presentation.checkout.components.PriceBreakdown
import shopzen.presentation.checkout.intent.CheckoutIntent
import shopzen.presentation.checkout.paymob.launchPaymobSdk
import shopzen.presentation.checkout.state.CheckoutState
import shopzen.presentation.checkout.viewmodel.CheckoutEffect
import shopzen.presentation.checkout.viewmodel.CheckoutViewModel
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.theme.LocalShopzenColors
import shopzen.presentation.common.theme.ShopzenBody
import shopzen.presentation.common.theme.ShopzenHeading3
import shopzen.presentation.common.theme.ShopzenMotion
import shopzen.presentation.common.theme.ShopzenShapes
import shopzen.presentation.common.theme.ShopzenSize
import shopzen.presentation.common.theme.ShopzenSmall
import shopzen.presentation.common.theme.ShopzenSpacing

@Composable
fun PaymentScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOrderConfirmation: (String, String) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val latestOnNavigateToLogin by rememberUpdatedState(onNavigateToLogin)
    val latestOnNavigateToOrderConfirmation by rememberUpdatedState(onNavigateToOrderConfirmation)

    LaunchedEffect(viewModel) {
        viewModel.processIntent(CheckoutIntent.LoadCheckout)
    }

    LaunchedEffect(viewModel, snackbarHostState, context) {
        viewModel.effects.collect { effect ->
            when (effect) {
                CheckoutEffect.NavigateToLogin -> latestOnNavigateToLogin()
                is CheckoutEffect.NavigateToOrderConfirmation -> {
                    latestOnNavigateToOrderConfirmation(effect.orderId, effect.orderNumber)
                }
                is CheckoutEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message.asString(context))
                CheckoutEffect.NavigateToAddAddress -> Unit
                CheckoutEffect.NavigateToPayment -> Unit
                CheckoutEffect.NavigateHome -> Unit
            }
        }
    }

    LaunchedEffect(state.pendingPaymobLaunch, viewModel, context) {
        val pendingLaunch = state.pendingPaymobLaunch ?: return@LaunchedEffect
        viewModel.processIntent(
            CheckoutIntent.ConsumePendingPaymobLaunch(pendingLaunch.intentionId)
        )
        launchPaymobSdk(
            context = context,
            clientSecret = pendingLaunch.clientSecret.value,
            publicKey = pendingLaunch.publicKey.value,
            paymobSdkListener = object : PaymobSdkListener {
                override fun onSuccess(payResponse: HashMap<String, String?>) {
                    Log.d("Tago", "onSuccess: ")
                    viewModel.processIntent(
                        CheckoutIntent.OnlinePaymentSucceeded(payResponse.toMap())
                    )
                }

                override fun onFailure(msg: String?) {
                    Log.d("Tago", "onFailure: ")
                    viewModel.processIntent(CheckoutIntent.OnlinePaymentFailed(msg))
                }

                override fun onPending() {
                    Log.d("Tago", "onPending: ")
                    viewModel.processIntent(CheckoutIntent.OnlinePaymentPending)
                }
            },
        )
    }

    PaymentContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun PaymentContent(
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
                title = stringResource(R.string.checkout_payment_title),
                onNavigateBack = onNavigateBack,
            )
        },
        bottomBar = {
            PaymentBottomBar(
                enabled = state.canPlaceOrder,
                isPlacingOrder = state.isPlacingOrder,
                isOnlinePaymentInProgress = state.isOnlinePaymentInProgress,
                onPlaceOrder = { onIntent(CheckoutIntent.RequestPlaceOrder) },
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = state,
            contentKey = { s ->
                when {
                    s.isLoading -> "loading"
                    s.availablePaymentMethods.isEmpty() -> "empty"
                    else -> "content"
                }
            },
            transitionSpec = {
                fadeIn(tween(ShopzenMotion.DurationNormal)) togetherWith
                    fadeOut(tween(ShopzenMotion.DurationNormal))
            },
            label = "paymentContent",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { current ->
            if (current.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = c.actionPrimaryBg)
                }
            } else {
                PaymentBody(
                    state = current,
                    onIntent = onIntent,
                )
            }
        }
    }

    if (state.showPlaceOrderDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.checkout_confirm_order_title),
            message = stringResource(R.string.checkout_confirm_order_body),
            confirmText = stringResource(R.string.checkout_confirm_order_confirm),
            dismissText = stringResource(R.string.checkout_confirm_order_cancel),
            onConfirm = { onIntent(CheckoutIntent.ConfirmPlaceOrder) },
            onDismiss = { onIntent(CheckoutIntent.DismissPlaceOrderDialog) },
        )
    }
}

@Composable
private fun PaymentBody(
    state: CheckoutState,
    onIntent: (CheckoutIntent) -> Unit,
) {
    val c = LocalShopzenColors.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(ShopzenSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.LG),
    ) {
        item {
            AnimatedVisibility(visible = state.error != null) {
                state.error?.let {
                    Text(
                        text = it.asString(),
                        style = ShopzenSmall,
                        color = LocalShopzenColors.current.textError,
                    )
                }
            }
        }

        item {
            CheckoutSection(
                titleContent = {
                    Text(
                        text = stringResource(R.string.checkout_payment_method_title),
                        style = ShopzenHeading3,
                        color = c.textPrimary,
                    )
                },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD)) {
                    state.availablePaymentMethods.forEach { method ->
                        PaymentMethodRow(
                            method = method,
                            selected = method == state.selectedPaymentMethod,
                            onClick = {
                                onIntent(CheckoutIntent.SelectPaymentMethod(method))
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
private fun PaymentBottomBar(
    enabled: Boolean,
    isPlacingOrder: Boolean,
    isOnlinePaymentInProgress: Boolean,
    onPlaceOrder: () -> Unit,
) {
    val c = LocalShopzenColors.current
    val busyMessage = when {
        isPlacingOrder -> stringResource(R.string.checkout_placing_order)
        isOnlinePaymentInProgress -> stringResource(R.string.checkout_online_payment_in_progress)
        else -> null
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.backgroundPrimary)
            .navigationBarsPadding()
            .padding(ShopzenSpacing.LG),
    ) {
        AnimatedVisibility(visible = busyMessage != null) {
            busyMessage?.let { message ->
                Text(
                    text = message,
                    style = ShopzenBody,
                    color = c.textSecondary,
                    modifier = Modifier.padding(bottom = ShopzenSpacing.SM),
                )
            }
        }
        Button(
            onClick = onPlaceOrder,
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
            AnimatedContent(
                targetState = isPlacingOrder || isOnlinePaymentInProgress,
                transitionSpec = {
                    fadeIn(tween(ShopzenMotion.DurationNormal)) togetherWith
                        fadeOut(tween(ShopzenMotion.DurationNormal))
                },
                label = "paymentButtonContent",
            ) { isBusy ->
                if (isBusy) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = c.actionDisabledFg,
                        )
                        Spacer(modifier = Modifier.width(ShopzenSpacing.SM))
                        Text(
                            text = stringResource(R.string.checkout_processing_payment),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.checkout_place_order),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
