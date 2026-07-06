package shopzen.presentation.checkout.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import shopzen.domain.checkout.model.PaymentMethod
import shopzen.domain.profile.model.Address
import shopzen.presentation.R
import shopzen.presentation.checkout.state.CheckoutItemUi
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenBorderWidth
import shopzen.presentation.theme.ShopzenCaption
import shopzen.presentation.theme.ShopzenHeading3
import shopzen.presentation.theme.ShopzenMotion
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSize
import shopzen.presentation.theme.ShopzenSmall
import shopzen.presentation.theme.ShopzenSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = ShopzenHeading3,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.checkout_back_cd),
                    tint = c.iconPrimary,
                )
            }
        },
    )
}

@Composable
fun CheckoutSection(
    titleContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val c = LocalShopzenColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(tween(ShopzenMotion.DurationNormal))
            .background(c.surfaceCard, ShopzenShapes.LG)
            .padding(ShopzenSpacing.LG),
        verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            titleContent()
            trailingContent?.invoke()
        }
        content()
    }
}

@Composable
fun CheckoutItemRow(
    item: CheckoutItemUi,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = stringResource(R.string.checkout_item_image_cd, item.title),
            modifier = Modifier
                .size(64.dp)
                .clip(ShopzenShapes.MD)
                .background(c.imagePlaceholder),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.XS),
        ) {
            Text(
                text = item.title,
                style = ShopzenBody,
                color = c.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = item.variantTitle,
                style = ShopzenSmall,
                color = c.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.checkout_quantity, item.quantity),
                style = ShopzenCaption,
                color = c.textTertiary,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.formattedLineTotal,
                style = ShopzenBody,
                color = c.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = item.formattedUnitPrice,
                style = ShopzenSmall,
                color = c.textSecondary,
            )
        }
    }
}

@Composable
fun PriceBreakdown(
    formattedSubtotal: String,
    formattedDiscount: String?,
    formattedTotal: String,
    appliedCouponLabel: String?,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(tween(ShopzenMotion.DurationNormal)),
        verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.SM),
    ) {
        PriceRow(
            label = stringResource(R.string.checkout_subtotal),
            value = formattedSubtotal,
        )
        AnimatedVisibility(visible = appliedCouponLabel != null) {
            appliedCouponLabel?.let {
                PriceRow(
                    label = stringResource(R.string.checkout_applied_coupon),
                    value = it,
                    labelColor = c.textSuccess,
                    valueColor = c.textSuccess,
                )
            }
        }
        AnimatedVisibility(visible = formattedDiscount != null) {
            formattedDiscount?.let {
                PriceRow(
                    label = stringResource(R.string.checkout_discount),
                    value = it,
                    labelColor = c.textSuccess,
                    valueColor = c.textSuccess,
                )
            }
        }
        HorizontalDivider(color = c.divider)
        PriceRow(
            label = stringResource(R.string.checkout_total),
            value = formattedTotal,
            labelWeight = FontWeight.Bold,
            valueWeight = FontWeight.Bold,
            valueStyleIsTotal = true,
        )
    }
}

@Composable
fun AddressChoiceCard(
    address: Address,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    val borderColor by animateColorAsState(
        targetValue = if (selected) c.borderSelected else c.borderDefault,
        animationSpec = tween(ShopzenMotion.DurationNormal),
        label = "addressBorder",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) c.backgroundSecondary else c.surfaceCard,
        animationSpec = tween(ShopzenMotion.DurationNormal),
        label = "addressContainer",
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = ShopzenShapes.LG,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (selected) ShopzenBorderWidth.Selected else ShopzenBorderWidth.Default,
            color = borderColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ShopzenSpacing.LG),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD),
        ) {
            Icon(
                imageVector = Icons.Outlined.LocalShipping,
                contentDescription = null,
                tint = if (selected) c.iconPrimary else c.iconSecondary,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.XS),
            ) {
                Text(
                    text = address.recipientName.ifBlank { address.label },
                    style = ShopzenBody,
                    color = c.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = address.addressSummary(),
                    style = ShopzenSmall,
                    color = c.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = c.actionPrimaryBg,
                    unselectedColor = c.iconSecondary,
                ),
            )
        }
    }
}

@Composable
fun PaymentMethodRow(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    val borderColor by animateColorAsState(
        targetValue = if (selected) c.borderSelected else c.borderDefault,
        animationSpec = tween(ShopzenMotion.DurationNormal),
        label = "paymentBorder",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) c.backgroundSecondary else c.surfaceCard,
        animationSpec = tween(ShopzenMotion.DurationNormal),
        label = "paymentContainer",
    )
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = ShopzenShapes.LG,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (selected) ShopzenBorderWidth.Selected else ShopzenBorderWidth.Default,
            color = borderColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ShopzenSpacing.LG),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ShopzenSpacing.MD),
        ) {
            Icon(
                imageVector = if (method == PaymentMethod.CASH_ON_DELIVERY) {
                    Icons.Outlined.Payments
                } else {
                    Icons.Outlined.CreditCard
                },
                contentDescription = stringResource(R.string.checkout_payment_selection_cd),
                tint = if (selected) c.iconPrimary else c.iconSecondary,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.XS),
            ) {
                Text(
                    text = method.localizedLabel(),
                    style = ShopzenBody,
                    color = c.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = method.localizedDescription(),
                    style = ShopzenSmall,
                    color = c.textSecondary,
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = c.actionPrimaryBg,
                    unselectedColor = c.iconSecondary,
                ),
            )
        }
    }
}

@Composable
fun OrderSuccessContent(
    orderNumber: String,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ShopzenSpacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ShopzenSpacing.LG),
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = c.actionSuccessBg,
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                modifier = Modifier.padding(ShopzenSpacing.LG),
                tint = c.actionSuccessFg,
            )
        }
        Text(
            text = stringResource(R.string.checkout_order_success_title),
            style = ShopzenHeading3,
            color = c.textPrimary,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.checkout_order_success_body, orderNumber),
            style = ShopzenBody,
            color = c.textSecondary,
        )
        Button(
            onClick = onContinueShopping,
            modifier = Modifier
                .fillMaxWidth()
                .height(ShopzenSize.ButtonHeight),
            shape = ShopzenShapes.MD,
            contentPadding = PaddingValues(horizontal = ShopzenSpacing.LG),
            colors = ButtonDefaults.buttonColors(
                containerColor = c.actionPrimaryBg,
                contentColor = c.actionPrimaryFg,
            ),
        ) {
            Text(
                text = stringResource(R.string.checkout_continue_shopping),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelColor: androidx.compose.ui.graphics.Color = LocalShopzenColors.current.textSecondary,
    valueColor: androidx.compose.ui.graphics.Color = LocalShopzenColors.current.textPrimary,
    labelWeight: FontWeight = FontWeight.Normal,
    valueWeight: FontWeight = FontWeight.SemiBold,
    valueStyleIsTotal: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ShopzenBody,
            color = labelColor,
            fontWeight = labelWeight,
        )
        Text(
            text = value,
            style = if (valueStyleIsTotal) ShopzenHeading3 else ShopzenBody,
            color = valueColor,
            fontWeight = valueWeight,
        )
    }
}

@Composable
private fun PaymentMethod.localizedLabel(): String =
    when (this) {
        PaymentMethod.CASH_ON_DELIVERY -> stringResource(R.string.checkout_payment_cash_on_delivery)
        PaymentMethod.ONLINE_PAYMENT -> stringResource(R.string.checkout_payment_online)
    }

@Composable
private fun PaymentMethod.localizedDescription(): String =
    when (this) {
        PaymentMethod.CASH_ON_DELIVERY -> stringResource(R.string.checkout_payment_cash_on_delivery_desc)
        PaymentMethod.ONLINE_PAYMENT -> stringResource(R.string.checkout_payment_online_desc)
    }

private fun Address.addressSummary(): String =
    listOf(addressLine1, addressLine2, city, stateOrProvince, postalCode, country)
        .filterNot { it.isNullOrBlank() }
        .joinToString()
