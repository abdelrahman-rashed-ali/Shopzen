package shopzen.presentation.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import shopzen.presentation.theme.LocalShopzenColors
import shopzen.presentation.theme.ShopzenBody
import shopzen.presentation.theme.ShopzenHeading3
import shopzen.presentation.theme.ShopzenShapes
import shopzen.presentation.theme.ShopzenSpacing
import shopzen.presentation.theme.ShopzenTheme

/**
 * Reusable confirmation dialog for all destructive actions per AGENTS.md Â§11.
 *
 * Usage:
 *   ConfirmationDialog(
 *       visible = state.showRemoveItemDialog,
 *       title = "Remove Item",
 *       message = "Remove this item from your cart?",
 *       onConfirm = { onIntent(CartIntent.ConfirmRemoveItem) },
 *       onDismiss = { onIntent(CartIntent.DismissRemoveItemDialog) },
 *   )
 */
@Composable
fun ConfirmationDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalShopzenColors.current.overlay),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = scaleIn(initialScale = 0.92f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.92f) + fadeOut(),
                ) {
                    DialogCard(
                        title = title,
                        message = message,
                        confirmText = confirmText,
                        dismissText = dismissText,
                        onConfirm = onConfirm,
                        onDismiss = onDismiss,
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogCard(
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalShopzenColors.current
    Column(
        modifier = Modifier
            .padding(horizontal = ShopzenSpacing.XL)
            .clip(ShopzenShapes.XXL)
            .background(c.surfaceDialog)
            .padding(ShopzenSpacing.XXL),
    ) {
        Text(
            text = title,
            style = ShopzenHeading3,
            color = c.textPrimary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(ShopzenSpacing.SM))
        Text(
            text = message,
            style = ShopzenBody,
            color = c.textSecondary,
        )
        Spacer(Modifier.height(ShopzenSpacing.XXL))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ShopzenSpacing.SM),
        ) {
            // Dismiss (ghost/secondary)
            androidx.compose.material3.OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = ShopzenShapes.Full,
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = c.textPrimary,
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = c.borderDefault,
                ),
            ) {
                Text(
                    text = dismissText,
                    style = ShopzenBody,
                    fontWeight = FontWeight.Medium,
                )
            }
            // Confirm (danger)
            androidx.compose.material3.Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                shape = ShopzenShapes.Full,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = c.actionDangerBg,
                    contentColor = c.actionDangerFg,
                ),
            ) {
                Text(
                    text = confirmText,
                    style = ShopzenBody,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmationDialogPreview() {
    ShopzenTheme {
        DialogCard(
            title = "Remove Item",
            message = "Are you sure you want to remove this item from your cart?",
            confirmText = "Remove",
            dismissText = "Cancel",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
