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
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import shopzen.domain.profile.model.Order
import shopzen.domain.profile.model.OrderLineItem
import shopzen.presentation.R
import shopzen.presentation.checkout.components.CheckoutTopBar
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.profile.intent.OrderDetailIntent
import shopzen.presentation.profile.state.OrderDetailState
import shopzen.presentation.profile.util.formatOrderDate
import shopzen.presentation.profile.viewmodel.OrderDetailViewModel
import shopzen.presentation.common.theme.LocalShopzenColors
import shopzen.presentation.common.theme.ShopzenBody
import shopzen.presentation.common.theme.ShopzenCaption
import shopzen.presentation.common.theme.ShopzenHeading3
import shopzen.presentation.common.theme.ShopzenMotion
import shopzen.presentation.common.theme.ShopzenShapes
import shopzen.presentation.common.theme.ShopzenSize
import shopzen.presentation.common.theme.ShopzenSmall
import shopzen.presentation.common.theme.ShopzenSpacing

@Composable
fun OrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.processIntent(OrderDetailIntent.LoadOrder(orderId))
    }

    OrderDetailContent(
        state = state,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
fun OrderDetailContent(
    state: OrderDetailState,
    onIntent: (OrderDetailIntent) -> Unit,
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
        topBar = {
            CheckoutTopBar(
                title = stringResource(R.string.order_detail_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = state,
            contentKey = { current ->
                when {
                    current.isLoading -> "loading"
                    current.error != null && current.order == null -> "error"
                    current.order == null -> "empty"
                    else -> "content"
                }
            },
            transitionSpec = {
                fadeIn(tween(ShopzenMotion.DurationNormal)) togetherWith
                    fadeOut(tween(ShopzenMotion.DurationNormal))
            },
            label = "orderDetailContent",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { current ->
            when {
                current.isLoading -> LoadingIndicator()
                current.error != null && current.order == null -> OrderDetailMessage(
                    title = current.error.asString(),
                    actionLabel = stringResource(R.string.order_detail_retry),
                    onAction = { onIntent(OrderDetailIntent.Retry) },
                )
                current.order == null -> OrderDetailMessage(
                    title = stringResource(R.string.order_detail_empty),
                    actionLabel = stringResource(R.string.order_detail_retry),
                    onAction = { onIntent(OrderDetailIntent.Retry) },
                )
                else -> OrderDetailLoadedContent(order = current.order)
            }
        }
    }
}

@Composable
private fun OrderDetailLoadedContent(
    order: Order,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ShopzenSpacing.LG,
            top = ShopzenSpacing.LG,
            end = ShopzenSpacing.LG,
            bottom = ShopzenSpacing.XXXL,
        ),
        verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.LG),
    ) {
        item {
            OrderDetailSummaryCard(
                order = order,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Text(
                text = stringResource(R.string.order_detail_items_title),
                style = ShopzenHeading3,
                color = LocalShopzenColors.current.textPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        items(order.lineItems, key = { it.id }) { item ->
            OrderLineItemRow(
                item = item,
                currency = order.currency,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OrderDetailSummaryCard(
    order: Order,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    val itemCount = remember(order.lineItems) { order.lineItems.sumOf { it.quantity } }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = c.surfaceCard),
        shape = ShopzenShapes.LG,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ShopzenSpacing.LG),
            verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD),
        ) {
            Text(
                text = stringResource(R.string.order_history_order_number, order.orderNumber),
                style = ShopzenHeading3,
                color = c.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = order.localizedDate(),
                style = ShopzenSmall,
                color = c.textSecondary,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ShopzenSpacing.SM),
            ) {
                OrderDetailStatusChip(
                    text = order.financialStatus.localizedFinancialStatus(),
                    modifier = Modifier.weight(1f),
                )
                OrderDetailStatusChip(
                    text = order.fulfillmentStatus.localizedFulfillmentStatus(),
                    modifier = Modifier.weight(1f),
                )
            }
            OrderDetailPriceRow(
                label = stringResource(R.string.order_history_payment_method),
                value = order.paymentMethod.localizedPaymentMethod(),
            )
            HorizontalDivider(color = c.divider)
            Text(
                text = pluralStringResource(
                    id = R.plurals.order_history_item_count,
                    count = itemCount,
                    itemCount,
                ),
                style = ShopzenBody,
                color = c.textSecondary,
            )
            OrderDetailPriceRow(
                label = stringResource(R.string.order_detail_subtotal),
                value = order.formattedSubtotal(),
            )
            if (order.discountAmount > 0.0) {
                OrderDetailPriceRow(
                    label = order.discountLabel(),
                    value = "-${order.formattedDiscount()}",
                    labelColor = c.textSuccess,
                    valueColor = c.textSuccess,
                )
            }
            OrderDetailPriceRow(
                label = stringResource(R.string.order_detail_total),
                value = order.formattedTotal(),
                valueStyleIsTotal = true,
            )
        }
    }
}

@Composable
private fun OrderLineItemRow(
    item: OrderLineItem,
    currency: String,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = c.surfaceCard),
        shape = ShopzenShapes.LG,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ShopzenSpacing.LG),
            horizontalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.XS),
            ) {
                Text(
                    text = item.title,
                    style = ShopzenBody,
                    color = c.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                item.variantTitle?.takeIf { it.isNotBlank() }?.let { variantTitle ->
                    Text(
                        text = variantTitle,
                        style = ShopzenSmall,
                        color = c.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = stringResource(R.string.order_detail_quantity, item.quantity),
                    style = ShopzenCaption,
                    color = c.textTertiary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(item.price * item.quantity, currency),
                    style = ShopzenBody,
                    color = c.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = formatCurrency(item.price, currency),
                    style = ShopzenSmall,
                    color = c.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun OrderDetailStatusChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    Text(
        text = text,
        modifier = modifier
            .background(c.backgroundSecondary, ShopzenShapes.Full)
            .padding(horizontal = ShopzenSpacing.MD, vertical = ShopzenSpacing.SM),
        style = ShopzenCaption,
        color = c.textSecondary,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun OrderDetailPriceRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: androidx.compose.ui.graphics.Color = LocalShopzenColors.current.textSecondary,
    valueColor: androidx.compose.ui.graphics.Color = LocalShopzenColors.current.textPrimary,
    valueStyleIsTotal: Boolean = false,
) {
    val c = LocalShopzenColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ShopzenBody,
            color = labelColor,
        )
        if (value.isNotBlank()) {
            Text(
                text = value,
                style = if (valueStyleIsTotal) ShopzenHeading3 else ShopzenBody,
                color = valueColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun OrderDetailMessage(
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

@Composable
private fun String.localizedFinancialStatus(): String =
    when (lowercase(Locale.ROOT)) {
        "paid" -> stringResource(R.string.order_history_status_paid)
        "pending" -> stringResource(R.string.order_history_status_pending)
        "authorized" -> stringResource(R.string.order_history_status_authorized)
        "refunded" -> stringResource(R.string.order_history_status_refunded)
        "partially_refunded" -> stringResource(R.string.order_history_status_partially_refunded)
        "voided" -> stringResource(R.string.order_history_status_voided)
        else -> stringResource(R.string.order_history_status_unknown)
    }

@Composable
private fun String.localizedFulfillmentStatus(): String =
    when (lowercase(Locale.ROOT)) {
        "fulfilled" -> stringResource(R.string.order_history_status_fulfilled)
        "partial" -> stringResource(R.string.order_history_status_partially_fulfilled)
        "unfulfilled", "unknown" -> stringResource(R.string.order_history_status_unfulfilled)
        else -> stringResource(R.string.order_history_status_unknown)
    }

@Composable
private fun String.localizedPaymentMethod(): String =
    when (uppercase(Locale.ROOT)) {
        "CASH_ON_DELIVERY" -> stringResource(R.string.checkout_payment_cash_on_delivery)
        "ONLINE_PAYMENT" -> stringResource(R.string.checkout_payment_online)
        "UNKNOWN", "UNKNOWN_STATUS", "UNKNOWN STATUS" -> stringResource(R.string.order_payment_unknown)
        else -> replace("_", " ")
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

@Composable
private fun Order.localizedDate(): String {
    val locale = currentLocale()
    return remember(createdAt, locale) { formatOrderDate(createdAt, locale) }
        ?: stringResource(R.string.order_history_date_unknown)
}

@Composable
private fun Order.discountLabel(): String =
    discountCode?.takeIf { it.isNotBlank() }?.let {
        stringResource(R.string.order_history_discount_with_code, it)
    } ?: stringResource(R.string.order_history_discount)

@Composable
private fun Order.formattedSubtotal(): String =
    formatCurrency(subtotalPrice, currency)

@Composable
private fun Order.formattedDiscount(): String =
    formatCurrency(discountAmount, currency)

@Composable
private fun Order.formattedTotal(): String =
    formatCurrency(totalPrice, currency)

@Composable
private fun formatCurrency(amount: Double, currency: String): String {
    val locale = currentLocale()
    return remember(amount, currency, locale) {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        runCatching { formatter.currency = Currency.getInstance(currency) }
        formatter.format(amount)
    }
}

@Composable
private fun currentLocale(): Locale {
    val configuration = LocalConfiguration.current
    return remember(configuration) { configuration.locales[0] }
}
