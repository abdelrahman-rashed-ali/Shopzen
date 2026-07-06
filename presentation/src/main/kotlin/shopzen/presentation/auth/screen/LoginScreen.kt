package shopzen.presentation.auth.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.R
import shopzen.presentation.auth.components.AuthMessage
import shopzen.presentation.auth.intent.LoginIntent
import shopzen.presentation.auth.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navigateToHome: () -> Unit,
    navigateToRegister: () -> Unit,
    navigateToForgotPassword: () -> Unit,
    launchGoogleSignIn: (
        onToken: (String) -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    launchAppleSignIn: (
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(
        state.isSignedIn,
        state.isGuest
    ) {
        if (state.isSignedIn || state.isGuest) {
            navigateToHome()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_logo),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(48.dp))

        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.sign_in_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(56.dp))

        LoginTextField(
            value = state.email,
            label = stringResource(R.string.email_address),
            hint = stringResource(R.string.email_hint),
            enabled = !state.isLoading,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            onValueChange = { viewModel.processIntent(LoginIntent.EmailChanged(it)) }
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.password),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(
                enabled = !state.isLoading,
                onClick = navigateToForgotPassword
            ) {
                Text(stringResource(R.string.forgot_password))
            }
        }

        LoginPasswordField(
            value = state.password,
            hint = stringResource(R.string.password_hint),
            enabled = !state.isLoading,
            onValueChange = { viewModel.processIntent(LoginIntent.PasswordChanged(it)) }
        )

        AnimatedVisibility(visible = state.error != null) {
            Column {
                Spacer(Modifier.height(16.dp))
                state.error?.let {
                    AuthMessage(
                        text = it,
                        error = true
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            enabled = !state.isLoading,
            onClick = { viewModel.processIntent(LoginIntent.Submit) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.sign_in),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        DividerText(text = stringResource(R.string.or_continue_with))

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SocialButton(
                label = stringResource(R.string.continue_with_google),
                mark = "G",
                enabled = !state.isLoading,
                modifier = Modifier.weight(1f),
                onClick = {
                    launchGoogleSignIn(
                        { token -> viewModel.processIntent(LoginIntent.SignInWithGoogle(token)) },
                        { message -> viewModel.processIntent(LoginIntent.SocialSignInFailed(message)) }
                    )
                }
            )
            SocialButton(
                label = stringResource(R.string.continue_with_apple),
                mark = "A",
                enabled = !state.isLoading,
                modifier = Modifier.weight(1f),
                onClick = {
                    launchAppleSignIn(
                        { viewModel.processIntent(LoginIntent.ExternalSignInSucceeded) },
                        { message -> viewModel.processIntent(LoginIntent.SocialSignInFailed(message)) }
                    )
                }
            )
        }

        Spacer(Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.dont_have_account),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                enabled = !state.isLoading,
                onClick = navigateToRegister
            ) {
                Text(
                    text = stringResource(R.string.register),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        TextButton(
            enabled = !state.isLoading,
            onClick = { viewModel.processIntent(LoginIntent.ContinueAsGuest) }
        ) {
            Text(
                text = stringResource(R.string.continue_as_guest),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoginTextField(
    value: String,
    label: String,
    hint: String,
    enabled: Boolean,
    leadingIcon: @Composable () -> Unit,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            leadingIcon = leadingIcon,
            placeholder = {
                Text(
                    text = hint,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            },
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
            singleLine = true,
            visualTransformation = VisualTransformation.None,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun LoginPasswordField(
    value: String,
    hint: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            val icon = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        placeholder = {
            Text(
                text = hint,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        },
        textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onSurface),
        singleLine = true,
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun DividerText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SocialButton(
    label: String,
    mark: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier.height(56.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
