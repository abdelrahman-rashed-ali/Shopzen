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
import androidx.compose.material3.HorizontalDivider
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
import shopzen.presentation.auth.components.GoogleSignInButton
import shopzen.presentation.auth.components.PasswordInputField
import shopzen.presentation.auth.intent.LoginIntent
import shopzen.presentation.auth.state.LoginState
import shopzen.presentation.auth.viewmodel.LoginViewModel
import shopzen.presentation.theme.Gray900
import shopzen.presentation.theme.Gray950

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
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedIn, state.isGuest) {
        if (state.isSignedIn || state.isGuest) {
            navigateToHome()
        }
    }

    LoginScreen(
        state = state,
        onIntent = viewModel::processIntent,
        navigateToRegister = navigateToRegister,
        navigateToForgotPassword = navigateToForgotPassword,
        launchGoogleSignIn = launchGoogleSignIn,
        launchAppleSignIn = launchAppleSignIn,
        modifier = modifier
    )
}

@Composable
private fun LoginScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
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
                        text = stringResource(R.string.sign_in_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            AuthTextField(
                value = state.email,
                label = stringResource(R.string.email_address),
                hint = stringResource(R.string.email_hint),
                enabled = !state.isLoading,
                onValueChange = { onIntent(LoginIntent.EmailChanged(it)) }
            )

            Spacer(Modifier.height(16.dp))

            PasswordInputField(
                value = state.password,
                label = stringResource(R.string.password),
                enabled = !state.isLoading,
                onChange = { onIntent(LoginIntent.PasswordChanged(it)) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    enabled = !state.isLoading,
                    onClick = navigateToForgotPassword
                ) {
                    Text(stringResource(R.string.forgot_password))
                }
            }

            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    state.error?.let {
                        AuthMessage(
                            text = it,
                            error = true
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            AuthButton(
                text = stringResource(R.string.sign_in),
                loading = state.isLoading,
                onClick = { onIntent(LoginIntent.Submit) }
            )

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "or",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(32.dp))

            GoogleSignInButton(
                label = stringResource(R.string.continue_with_google),
                enabled = !state.isLoading,
                onClick = {
                    launchGoogleSignIn(
                        { token -> onIntent(LoginIntent.SignInWithGoogle(token)) },
                        { message -> onIntent(LoginIntent.SocialSignInFailed(message)) }
                    )
                }
            )

            Spacer(Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                onClick = { onIntent(LoginIntent.ContinueAsGuest) }
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
}
