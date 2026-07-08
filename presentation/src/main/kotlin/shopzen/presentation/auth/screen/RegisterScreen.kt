package shopzen.presentation.auth.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.R
import shopzen.presentation.auth.components.AuthButton
import shopzen.presentation.auth.components.AuthMessage
import shopzen.presentation.auth.components.AuthTextField
import shopzen.presentation.auth.components.PasswordInputField
import shopzen.presentation.auth.intent.RegisterIntent
import shopzen.presentation.auth.state.RegisterState
import shopzen.presentation.auth.viewmodel.RegisterViewModel
import shopzen.presentation.theme.Gray900
import shopzen.presentation.theme.Gray950

@Composable
fun RegisterScreen(
    navigateToEmailVerification: () -> Unit,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) {
            navigateToEmailVerification()
        }
    }

    RegisterScreen(
        state = state,
        onIntent = viewModel::processIntent,
        navigateToLogin = navigateToLogin,
        modifier = modifier
    )
}

@Composable
private fun RegisterScreen(
    state: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    navigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Gray950, Gray900)))
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.app_logo),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.create_account_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            AuthTextField(
                value = state.name,
                label = stringResource(R.string.full_name),
                hint = stringResource(R.string.name_hint),
                enabled = !state.isLoading,
                onValueChange = { onIntent(RegisterIntent.NameChanged(it)) }
            )

            Spacer(Modifier.height(16.dp))

            AuthTextField(
                value = state.email,
                label = stringResource(R.string.email_address),
                hint = stringResource(R.string.email_hint),
                enabled = !state.isLoading,
                onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) }
            )

            Spacer(Modifier.height(16.dp))

            PasswordInputField(
                value = state.password,
                label = stringResource(R.string.password),
                enabled = !state.isLoading,
                onChange = { onIntent(RegisterIntent.PasswordChanged(it)) }
            )

            Spacer(Modifier.height(16.dp))

            PasswordInputField(
                value = state.confirmPassword,
                label = stringResource(R.string.confirm_password),
                enabled = !state.isLoading,
                onChange = { onIntent(RegisterIntent.ConfirmPasswordChanged(it)) }
            )

            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
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

            AuthButton(
                text = stringResource(R.string.sign_up),
                loading = state.isLoading,
                onClick = { onIntent(RegisterIntent.Submit) }
            )

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.already_have_account),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    enabled = !state.isLoading,
                    onClick = navigateToLogin
                ) {
                    Text(
                        text = stringResource(R.string.login),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
