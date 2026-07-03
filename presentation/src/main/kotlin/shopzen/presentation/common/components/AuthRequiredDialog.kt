package shopzen.presentation.common.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import shopzen.presentation.R
import shopzen.presentation.theme.LocalShopzenColors

@Composable
fun AuthRequiredDialog(
    onLoginClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalShopzenColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.auth_required_title)) },
        text = { Text(stringResource(R.string.auth_required_message)) },
        confirmButton = {
            TextButton(onClick = onLoginClick) {
                Text(text = stringResource(R.string.auth_required_login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.auth_required_cancel))
            }
        },
        containerColor = colors.surfaceDialog,
        titleContentColor = colors.textPrimary,
        textContentColor = colors.textSecondary,
    )
}
