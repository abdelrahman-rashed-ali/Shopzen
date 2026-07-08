package shopzen.presentation.cart.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import shopzen.presentation.R
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenHeading3
import shopzen.presentation.theme.ShopzenMotion
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSmall
import shopzen.presentation.theme.ShopzenSpacing
import shopzen.presentation.theme.ShopzenTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponBottomSheet(
    visible: Boolean,
    couponCode: String,
    couponError: String?,
    isCouponLoading: Boolean,
    sheetState: SheetState,
    onCodeChange: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val c = LocalShopzenColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surfaceDialog,
        shape = ShopzenShapes.BottomSheet,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .background(
                        color = c.borderDefault,
                        shape = ShopzenShapes.Full,
                    )
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ShopzenSpacing.XL)
                .navigationBarsPadding()
                .padding(bottom = ShopzenSpacing.XXL),
        ) {
            Spacer(Modifier.height(ShopzenSpacing.LG))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.cart_add_coupon),
                    style = ShopzenHeading3,
                    color = c.textPrimary,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.cart_close),
                        tint = c.iconSecondary,
                    )
                }
            }

            Spacer(Modifier.height(ShopzenSpacing.LG))

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

            Spacer(Modifier.height(ShopzenSpacing.LG))
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
            trackColor = c.backgroundSecondary,
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun CouponBottomSheetPreview() {
    ShopzenTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ApplyButton(isLoading = false, enabled = true, onClick = {})
            ApplyButton(isLoading = true, enabled = false, onClick = {})
        }
    }
}
