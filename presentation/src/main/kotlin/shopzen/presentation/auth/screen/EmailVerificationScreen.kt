package shopzen.presentation.auth.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.R
import shopzen.presentation.auth.components.AuthButton
import shopzen.presentation.auth.components.AuthMessage
import shopzen.presentation.auth.state.RegisterState
import shopzen.presentation.auth.viewmodel.RegisterViewModel
import shopzen.presentation.theme.Gray900
import shopzen.presentation.theme.Gray950

@Composable
fun EmailVerificationScreen(
    navigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isEmailVerified) {
        if (state.isEmailVerified) {
            navigateHome()
        }
    }

    EmailVerificationScreen(
        state = state,
        onCheckVerification = viewModel::checkEmailVerified,
        onResendEmail = viewModel::resendVerificationEmail,
        modifier = modifier
    )
}

@Composable
private fun EmailVerificationScreen(
    state: RegisterState,
    onCheckVerification: () -> Unit,
    onResendEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    var resent by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Gray950, Gray900)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .drawBehind {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.2f),
                            radius = size.width / 2 * pulse
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.4f),
                            radius = size.width / 2 * (pulse * 0.8f)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.verify_email),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.verify_email_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

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

            AnimatedVisibility(
                visible = resent && state.error == null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    AuthMessage(
                        text = stringResource(R.string.email_resent),
                        error = false
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            AuthButton(
                text = stringResource(R.string.continue_text),
                loading = state.isLoading,
                onClick = {
                    resent = false
                    onCheckVerification()
                }
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    onResendEmail()
                    resent = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.resend_email),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
