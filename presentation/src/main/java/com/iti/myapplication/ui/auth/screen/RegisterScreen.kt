package com.shopzen.presentation.auth.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopzen.presentation.navigation.Routes
import com.shopzen.presentation.auth.intent.RegisterIntent
import com.shopzen.presentation.auth.viewmodel.RegisterViewModel

// ── Design tokens (Obsidian & Gold) ──────────────────────────────────────────

private val ColorPrimary          = Color(0xFF000000)   // Obsidian
private val ColorOnPrimary        = Color(0xFFFFFFFF)
private val ColorSurface          = Color(0xFFF9F9F9)
private val ColorSurfaceContainer = Color(0xFFEEEEEE)
private val ColorSurfaceContainerLow = Color(0xFFF3F3F3)
private val ColorOnSurface        = Color(0xFF1A1C1C)
private val ColorOnSurfaceVariant = Color(0xFF444748)
private val ColorOutline          = Color(0xFF747878)
private val ColorOutlineVariant   = Color(0xFFC4C7C7)
private val ColorError            = Color(0xFFBA1A1A)

private val RadiusDefault = 8.dp   // rounded-lg / DEFAULT from design
private val RadiusInputTop = 4.dp  // rounded-t-DEFAULT

private val SpacingStackSm = 8.dp
private val SpacingStackMd = 16.dp
private val SpacingStackLg = 32.dp
private val SpacingMarginMobile = 20.dp

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Navigate to email verification once registration succeeds
    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) {
            navController.navigate(Routes.EMAIL_VERIFICATION) {
                popUpTo(Routes.REGISTER) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorSurface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingMarginMobile)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(SpacingStackLg))

            // ── Logo placeholder ──────────────────────────────────────────
            Text(
                text = "LUMINA",
                fontSize = 20.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = 0.16.sp,
                color = ColorOnSurface
            )

            Spacer(Modifier.height(SpacingStackLg))

            // ── Card ──────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(RadiusDefault),
                color = Color.White,
                shadowElevation = 4.dp,
                tonalElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SpacingStackLg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ── Headline ─────────────────────────────────────────
                    Text(
                        text = "Create Account",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.32).sp,
                        color = ColorOnSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(SpacingStackSm))

                    Text(
                        text = "Enter your details to begin your journey.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = ColorOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(SpacingStackLg))

                    // ── Fields ───────────────────────────────────────────
                    val focusManager = LocalFocusManager.current

                    LuminaInputField(
                        label = "Full Name",
                        value = state.name,
                        onValueChange = { viewModel.processIntent(RegisterIntent.NameChanged(it)) },
                        placeholder = "e.g. Jane Doe",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        enabled = !state.isLoading
                    )

                    Spacer(Modifier.height(SpacingStackMd))

                    LuminaInputField(
                        label = "Email Address",
                        value = state.email,
                        onValueChange = { viewModel.processIntent(RegisterIntent.EmailChanged(it)) },
                        placeholder = "name@example.com",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        enabled = !state.isLoading
                    )

                    Spacer(Modifier.height(SpacingStackMd))

                    PasswordInputField(
                        label = "Password",
                        value = state.password,
                        onValueChange = { viewModel.processIntent(RegisterIntent.PasswordChanged(it)) },
                        imeAction = ImeAction.Next,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        enabled = !state.isLoading
                    )

                    Spacer(Modifier.height(SpacingStackMd))

                    PasswordInputField(
                        label = "Confirm Password",
                        value = state.confirmPassword,
                        onValueChange = { viewModel.processIntent(RegisterIntent.ConfirmPasswordChanged(it)) },
                        imeAction = ImeAction.Done,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.processIntent(RegisterIntent.Submit)
                            }
                        ),
                        enabled = !state.isLoading
                    )

                    Spacer(Modifier.height(SpacingStackMd))

                    // ── Inline error ─────────────────────────────────────
                    AnimatedVisibility(
                        visible = state.error != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        state.error?.let { errorMsg ->
                            Text(
                                text = errorMsg,
                                fontSize = 12.sp,
                                color = ColorError,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = SpacingStackSm)
                            )
                        }
                    }

                    Spacer(Modifier.height(SpacingStackLg))

                    // ── Sign Up Button ───────────────────────────────────
                    Button(
                        onClick = { viewModel.processIntent(RegisterIntent.Submit) },
                        enabled = !state.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(RadiusDefault),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorPrimary,
                            contentColor = ColorOnPrimary,
                            disabledContainerColor = ColorPrimary.copy(alpha = 0.5f),
                            disabledContentColor = ColorOnPrimary.copy(alpha = 0.6f)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ColorOnPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "SIGN UP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.96.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(SpacingStackLg))

                    // ── Log in link ──────────────────────────────────────
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            fontSize = 14.sp,
                            color = ColorOnSurfaceVariant
                        )
                        TextButton(
                            onClick = { navController.popBackStack() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Log in",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ColorOnSurface,
                                textDecoration = TextDecoration.None
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(SpacingStackLg))
        }
    }
}

// ── Reusable input components ─────────────────────────────────────────────────

@Composable
private fun LuminaInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ColorOnSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = ColorOutlineVariant
                )
            },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = true,
            shape = RoundedCornerShape(topStart = RadiusInputTop, topEnd = RadiusInputTop),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ColorSurfaceContainerLow,
                unfocusedContainerColor = ColorSurfaceContainerLow,
                disabledContainerColor = ColorSurfaceContainerLow,
                focusedTextColor = ColorOnSurface,
                unfocusedTextColor = ColorOnSurface,
                cursorColor = ColorOnSurface,
                focusedIndicatorColor = ColorOnSurface,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions,
    enabled: Boolean
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = ColorOnSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled,
            placeholder = {
                Text(
                    text = "••••••••",
                    fontSize = 14.sp,
                    color = ColorOutlineVariant
                )
            },
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Outlined.VisibilityOff
                        else
                            Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = ColorOnSurfaceVariant
                    )
                }
            },
            shape = RoundedCornerShape(topStart = RadiusInputTop, topEnd = RadiusInputTop),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ColorSurfaceContainerLow,
                unfocusedContainerColor = ColorSurfaceContainerLow,
                disabledContainerColor = ColorSurfaceContainerLow,
                focusedTextColor = ColorOnSurface,
                unfocusedTextColor = ColorOnSurface,
                cursorColor = ColorOnSurface,
                focusedIndicatorColor = ColorOnSurface,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}
