package shopzen.presentation.cart.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenHeading3
import shopzen.presentation.theme.ShopzenRadius
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSmall
import shopzen.presentation.theme.ShopzenSpacing
import shopzen.presentation.theme.ShopzenTheme

/**
 * Cart summary bottom section showing:
 *  - "Add Coupon" chip (tappable â†’ opens CouponBottomSheet)
 *  - Applied coupon chip with remove [X]
 *  - Divider
 *  - Subtotal row
 *  - Discount row (AnimatedVisibility â€” appears when coupon applied)
 *  - Total row (bolder)
 */
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
        HorizontalDivider(color = c.divider, thickness = 1.dp)

        Spacer(Modifier.height(ShopzenSpacing.LG))

        // Coupon area
        if (couponApplied && appliedCouponLabel != null) {
            AppliedCouponChip(
                label = appliedCouponLabel,
                onRemove = onRemoveCoupon,
            )
        } else {
            AddCouponChip(onClick = onAddCoupon)
        }

        Spacer(Modifier.height(ShopzenSpacing.LG))

        HorizontalDivider(color = c.divider, thickness = 1.dp)

        Spacer(Modifier.height(ShopzenSpacing.LG))

        // Subtotal
        SummaryRow(
            label = "Subtotal",
            value = formattedSubtotal,
            labelWeight = FontWeight.Normal,
            valueWeight = FontWeight.SemiBold,
        )

        // Discount row â€” animated insertion
        AnimatedVisibility(
            visible = formattedDiscount != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            formattedDiscount?.let {
                Spacer(Modifier.height(ShopzenSpacing.SM))
                SummaryRow(
                    label = "Discount",
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

        // Total â€” heavier weight
        SummaryRow(
            label = "Total",
            value = formattedTotal,
            labelWeight = FontWeight.SemiBold,
            valueWeight = FontWeight.Bold,
            isTotal = true,
        )

        Spacer(Modifier.height(ShopzenSpacing.LG))
    }
}

@Composable
private fun AddCouponChip(onClick: () -> Unit) {
    val c = LocalShopzenColors.current
    Row(
        modifier = Modifier
            .clip(ShopzenShapes.Full)
            .border(
                width = 1.dp,
                color = c.borderDefault,
                shape = ShopzenShapes.Full,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = ShopzenSpacing.MD, vertical = ShopzenSpacing.SM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShopzenSpacing.XS),
    ) {
        Icon(
            imageVector = Icons.Outlined.ConfirmationNumber,
            contentDescription = null,
            tint = c.iconSecondary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "Add Coupon",
            style = ShopzenSmall,
            color = c.textSecondary,
        )
    }
}

@Composable
private fun AppliedCouponChip(
    label: String,
    onRemove: () -> Unit,
) {
    val c = LocalShopzenColors.current
    Row(
        modifier = Modifier
            .clip(ShopzenShapes.Full)
            .background(c.actionSuccessBg)
            .border(
                width = 1.dp,
                color = c.borderSuccess,
                shape = ShopzenShapes.Full,
            )
            .padding(start = ShopzenSpacing.MD, end = ShopzenSpacing.XS, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.ConfirmationNumber,
            contentDescription = null,
            tint = c.actionSuccessFg,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.size(ShopzenSpacing.XS))
        Text(
            text = label,
            style = ShopzenSmall,
            fontWeight = FontWeight.Medium,
            color = c.actionSuccessFg,
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Remove coupon",
                tint = c.actionSuccessFg,
                modifier = Modifier.size(14.dp),
            )
        }
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
            style = if (isTotal) ShopzenBody else ShopzenBody,
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
