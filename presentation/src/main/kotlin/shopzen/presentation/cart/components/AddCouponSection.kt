package shopzen.presentation.cart.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Discount
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import shopzen.presentation.R
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenElevation
import shopzen.presentation.theme.ShopzenMotion
import shopzen.presentation.theme.ShopzenSectionTitle
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSmall
import shopzen.presentation.theme.ShopzenSpacing

@Composable
fun AddCouponSection(
    couponCode: String,
    couponError: String?,
    couponApplied: Boolean,
    appliedCouponLabel: String?,
    isCouponLoading: Boolean,
    onCodeChange: (String) -> Unit,
    onApply: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalShopzenColors.current

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
    ) {
        if (couponApplied) {
            // Applied Coupon State
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ShopzenSpacing.LG),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Discount,
                    contentDescription = null,
                    tint = c.textSuccess,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(ShopzenSpacing.MD))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.cart_coupon_applied),
                        style = ShopzenSectionTitle,
                        color = c.textSuccess,
                    )
                    appliedCouponLabel?.let {
                        Text(
                            text = it,
                            style = ShopzenBody,
                            color = c.textSecondary,
                        )
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.cart_remove),
                        tint = c.iconSecondary,
                    )
                }
            }
        } else {
            // Input State
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ShopzenSpacing.LG),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Discount,
                        contentDescription = null,
                        tint = c.iconPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(ShopzenSpacing.SM))
                    Text(
                        text = stringResource(R.string.cart_apply_coupon),
                        style = ShopzenSectionTitle,
                        color = c.textPrimary,
                    )
                }

                Spacer(Modifier.height(ShopzenSpacing.SM))

                val borderColor by animateColorAsState(
                    targetValue = when {
                        couponError != null -> c.borderError
                        else -> c.borderDefault
                    },
                    animationSpec = tween(ShopzenMotion.DurationNormal),
                    label = "couponBorder",
                )

                OutlinedTextField(
                    value = couponCode,
                    onValueChange = onCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.cart_coupon_hint),
                            style = ShopzenBody,
                            color = c.textPlaceholder,
                        )
                    },
                    trailingIcon = {
                        ApplyButton(
                            isLoading = isCouponLoading,
                            onClick = onApply,
                            enabled = couponCode.isNotBlank() && !isCouponLoading,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onApply() }),
                    shape = ShopzenShapes.MD,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = c.borderFocus,
                        unfocusedBorderColor = borderColor,
                        errorBorderColor = c.borderError,
                        focusedContainerColor = c.surfaceInput,
                        unfocusedContainerColor = c.surfaceInput,
                        cursorColor = c.textPrimary,
                        focusedTextColor = c.textPrimary,
                        unfocusedTextColor = c.textPrimary,
                    ),
                )

                AnimatedVisibility(
                    visible = couponError != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    couponError?.let {
                        Spacer(Modifier.height(ShopzenSpacing.XS))
                        Text(
                            text = it,
                            style = ShopzenSmall,
                            color = c.textError,
                            modifier = Modifier.padding(start = ShopzenSpacing.XS),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplyButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val c = LocalShopzenColors.current
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(20.dp)
                .padding(end = 4.dp),
            strokeWidth = 2.dp,
            strokeCap = StrokeCap.Round,
            color = c.textLink,
            trackColor = Color.Transparent,
        )
    } else {
        TextButton(
            onClick = onClick,
            enabled = enabled,
        ) {
            Text(
                text = stringResource(R.string.cart_apply),
                style = ShopzenBody,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) c.textLink else c.textDisabled,
            )
        }
    }
}
