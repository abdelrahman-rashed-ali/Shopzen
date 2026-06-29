package shopzen.presentation.auth.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth


@Composable
fun PasswordInputField(
    value: String, label: String, onChange: (String) -> Unit
) {
    var visible by remember {
        mutableStateOf(false)
    }
    TextField(
        value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(), label = {
        Text(
            text = label, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }, placeholder = {
        Text(
            text = "••••••••", color = MaterialTheme.colorScheme.outlineVariant
        )
    }, textStyle = LocalTextStyle.current.copy(
        color = MaterialTheme.colorScheme.onSurface
    ), visualTransformation = if (visible) VisualTransformation.None
    else PasswordVisualTransformation(), trailingIcon = {
        IconButton(
            onClick = {
                visible = !visible
            }) {
            Icon(
                imageVector = if (visible) Icons.Outlined.VisibilityOff
                else Icons.Outlined.Visibility,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    },


        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.onSurface,

            focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
            unfocusedIndicatorColor = Color.Transparent

        )

    )

}
