package shopzen.presentation.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import shopzen.domain.profile.model.Order
import shopzen.presentation.R
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenCaption
import shopzen.presentation.theme.ShopzenHeading3
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSmall
import shopzen.presentation.theme.ShopzenSpacing
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

@Composable
fun OrderHistoryCard(
    order: Order,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    val itemCount = remember(order.lineItems) { order.lineItems.sumOf { it.quantity } }
    Card(
        onClick = onClick,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.XS)) {
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
                }
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = stringResource(R.string.order_history_open_order_cd),
                    tint = c.iconSecondary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ShopzenSpacing.SM),
            ) {
                OrderStatusChip(
                    text = order.financialStatus.localizedFinancialStatus(),
                    modifier = Modifier.weight(1f),
                )
                OrderStatusChip(
                    text = order.fulfillmentStatus.localizedFulfillmentStatus(),
                    modifier = Modifier.weight(1f),
                )
            }

            OrderInfoRow(
                label = stringResource(R.string.order_history_payment_method),
                value = order.paymentMethod.localizedPaymentMethod(),
            )

            if (order.discountAmount > 0.0) {
                OrderInfoRow(
                    label = order.discountLabel(),
                    value = "-${order.formattedDiscount()}",
                    valueColor = c.textSuccess,
                )
            }

            HorizontalDivider(color = c.divider)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                        text = pluralStringResource(
                            id = R.plurals.order_history_item_count,
                            count = itemCount,
                            itemCount,
                        ),
                    style = ShopzenBody,
                    color = c.textSecondary,
                )
                Text(
                    text = order.formattedTotal(),
                    style = ShopzenBody,
                    color = c.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun OrderInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = LocalShopzenColors.current.textPrimary,
) {
    val c = LocalShopzenColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ShopzenSmall,
            color = c.textSecondary,
        )
        Text(
            text = value,
            style = ShopzenSmall,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun OrderStatusChip(
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
        "UNKNOWN", "UNKNOWN_STATUS", "UNKNOWN STATUS", "UNKNOWN_STATUS" -> {
            stringResource(R.string.order_payment_unknown)
        }
        else -> replace("_", " ")
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }

@Composable
private fun Order.localizedDate(): String {
    val locale = currentLocale()
    return remember(createdAt, locale) {
        runCatching {
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale)
                .format(Instant.parse(createdAt).atZone(ZoneId.systemDefault()))
        }.getOrNull()
    } ?: stringResource(R.string.order_history_date_unknown)
}

@Composable
private fun Order.discountLabel(): String =
    discountCode?.takeIf { it.isNotBlank() }?.let {
        stringResource(R.string.order_history_discount_with_code, it)
    } ?: stringResource(R.string.order_history_discount)

@Composable
private fun Order.formattedTotal(): String {
    val locale = currentLocale()
    return remember(totalPrice, currency, locale) {
        val formatter = NumberFormat.getCurrencyInstance(locale)
        runCatching { formatter.currency = Currency.getInstance(currency) }
        formatter.format(totalPrice)
    }
}

@Composable
private fun Order.formattedDiscount(): String =
    formatCurrency(discountAmount, currency)

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
