package shopzen.presentation.cart.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Discount
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import shopzen.presentation.R
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenElevation
import shopzen.presentation.theme.ShopzenMotion
import shopzen.presentation.theme.ShopzenSectionTitle
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSpacing

@Composable
fun AddCouponSection(modifier: Modifier = Modifier, onAddCoupon: () -> Unit) {
    val c = LocalShopzenColors.current

    val cardInteraction = remember { MutableInteractionSource() }
    val cardPressed by cardInteraction.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (cardPressed) ShopzenMotion.ScaleCard else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = ShopzenElevation.SM,
                shape = ShopzenShapes.MD,
                ambientColor = c.shadowSm,
                spotColor = c.shadowSm,
            )
            .clip(ShopzenShapes.MD)
            .background(c.surfaceCard)
            .clickable(
                interactionSource = cardInteraction,
                indication = null,
                onClick = onAddCoupon,
            )
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ShopzenSpacing.LG),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Discount,
                contentDescription = stringResource(R.string.cart_apply_coupon),
                tint = c.iconPrimary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(ShopzenSpacing.MD))
            Column {
                Text(
                    text = stringResource(R.string.cart_apply_coupon),
                    style = ShopzenSectionTitle,
                    color = c.textPrimary,
                )
                Text(
                    text = stringResource(R.string.cart_coupon_desc),
                    style = ShopzenBody,
                    color = c.textSecondary,
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = stringResource(R.string.cart_apply_coupon),
                tint = c.iconPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
