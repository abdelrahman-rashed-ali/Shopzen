package shopzen.presentation.common.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import shopzen.presentation.R

@Composable
fun AuthRequiredDialog(
    onLoginClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        title = stringResource(R.string.auth_required_title),
        message = stringResource(R.string.auth_required_message),
        confirmText = stringResource(R.string.auth_required_login),
        cancelText = stringResource(R.string.auth_required_cancel),
        onConfirm = onLoginClick,
        onDismiss = onDismiss
    )
}
