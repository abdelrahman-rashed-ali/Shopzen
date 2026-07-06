package shopzen.presentation.cart.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import shopzen.presentation.R
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenHeading3
import shopzen.presentation.theme.ShopzenSpacing
import shopzen.presentation.theme.ShopzenTheme

@Composable
fun CartSummarySection(
    formattedSubtotal: String,
    formattedTotal: String,
    formattedDiscount: String?,
    couponApplied: Boolean,
    appliedCouponLabel: String?,
    onAddCoupon: () -> Unit,
    onRemoveCoupon: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(c.backgroundPrimary),
    ) {

        Spacer(Modifier.height(ShopzenSpacing.LG))

        if (couponApplied && appliedCouponLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ShopzenSpacing.XL)
                    .background(c.surfaceDialog, shape = shopzen.presentation.theme.ShopzenShapes.SM)
                    .padding(start = ShopzenSpacing.MD, end = ShopzenSpacing.XS, top = ShopzenSpacing.XS, bottom = ShopzenSpacing.XS),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appliedCouponLabel,
                    style = ShopzenBody,
                    color = c.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onRemoveCoupon) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.cart_remove_coupon_cd),
                        tint = c.iconSecondary
                    )
                }
            }
            Spacer(Modifier.height(ShopzenSpacing.MD))
        } else {
            TextButton(
                onClick = onAddCoupon,
                modifier = Modifier.padding(horizontal = ShopzenSpacing.MD)
            ) {
                Text(
                    text = stringResource(R.string.cart_add_coupon),
                    style = ShopzenBody,
                    color = c.textLink,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(ShopzenSpacing.MD))
        }

        SummaryRow(
            label = stringResource(R.string.cart_subtotal),
            value = formattedSubtotal,
            labelWeight = FontWeight.Normal,
            valueWeight = FontWeight.SemiBold,
        )

        AnimatedVisibility(
            visible = formattedDiscount != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            formattedDiscount?.let {
                Spacer(Modifier.height(ShopzenSpacing.SM))
                SummaryRow(
                    label = stringResource(R.string.cart_discount),
                    value = it,
                    labelColor = c.textSuccess,
                    valueColor = c.textSuccess,
                    labelWeight = FontWeight.Normal,
                    valueWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(ShopzenSpacing.SM))

        HorizontalDivider(color = c.divider, thickness = 1.dp)

        Spacer(Modifier.height(ShopzenSpacing.SM))

        SummaryRow(
            label = stringResource(R.string.cart_total),
            value = formattedTotal,
            labelWeight = FontWeight.SemiBold,
            valueWeight = FontWeight.Bold,
            isTotal = true,
        )

        Spacer(Modifier.height(ShopzenSpacing.LG))
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    labelWeight: FontWeight,
    valueWeight: FontWeight,
    labelColor: androidx.compose.ui.graphics.Color = LocalShopzenColors.current.textSecondary,
    valueColor: androidx.compose.ui.graphics.Color = LocalShopzenColors.current.textPrimary,
    isTotal: Boolean = false,
) {
    val c = LocalShopzenColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ShopzenSpacing.XL),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ShopzenBody,
            fontWeight = labelWeight,
            color = if (isTotal) c.textPrimary else labelColor,
        )
        Text(
            text = value,
            style = if (isTotal) ShopzenHeading3 else ShopzenBody,
            fontWeight = valueWeight,
            color = valueColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CartSummarySectionPreview() {
    ShopzenTheme {
        Column {
            CartSummarySection(
                formattedSubtotal = "$245.00",
                formattedTotal = "$220.50",
                formattedDiscount = "-$24.50",
                couponApplied = true,
                appliedCouponLabel = "SAVE10 (-10%)",
                onAddCoupon = {},
                onRemoveCoupon = {},
            )
        }
    }
}
