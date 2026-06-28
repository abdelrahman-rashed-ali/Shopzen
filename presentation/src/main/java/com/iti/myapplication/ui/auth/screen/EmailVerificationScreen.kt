package com.shopzen.presentation.auth.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.shopzen.presentation.navigation.Routes
import com.shopzen.presentation.auth.viewmodel.RegisterViewModel

// ── Design tokens (mirrors RegisterScreen) ────────────────────────────────────

private val ColorPrimary          = Color(0xFF000000)
private val ColorOnPrimary        = Color(0xFFFFFFFF)
private val ColorSurface          = Color(0xFFF9F9F9)
private val ColorOnSurface        = Color(0xFF1A1C1C)
private val ColorOnSurfaceVariant = Color(0xFF444748)
private val ColorError            = Color(0xFFBA1A1A)
private val ColorOutlineVariant   = Color(0xFFC4C7C7)

private val RadiusDefault = 8.dp
private val SpacingStackSm = 8.dp
private val SpacingStackMd = 16.dp
private val SpacingStackLg = 32.dp
private val SpacingMarginMobile = 20.dp

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EmailVerificationScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var notVerifiedError by remember { mutableStateOf(false) }
    var resentSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorSurface),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingMarginMobile),
            shape = RoundedCornerShape(RadiusDefault),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpacingStackLg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Headline ─────────────────────────────────────────────
                Text(
                    text = "Verify your\nEmail",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.32).sp,
                    color = ColorOnSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(SpacingStackMd))

                Text(
                    text = "We've sent a verification link to your email address. Please check your inbox and click the link to activate your account.",
                    fontSize = 14.sp,
                    color = ColorOnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(SpacingStackLg))

                // ── Resend success ───────────────────────────────────────
                AnimatedVisibility(visible = resentSuccess, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        text = "Verification email resent.",
                        fontSize = 12.sp,
                        color = Color(0xFF1B6B3A),
                        modifier = Modifier.padding(bottom = SpacingStackMd)
                    )
                }

                // ── Not-verified error ───────────────────────────────────
                AnimatedVisibility(visible = notVerifiedError, enter = fadeIn(), exit = fadeOut()) {
                    Text(
                        text = "Email not yet verified. Please check your inbox.",
                        fontSize = 12.sp,
                        color = ColorError,
                        modifier = Modifier.padding(bottom = SpacingStackMd)
                    )
                }

                // ── Continue button ──────────────────────────────────────
                Button(
                    onClick = {
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        firebaseUser?.reload()
                        if (firebaseUser?.isEmailVerified == true) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        } else {
                            notVerifiedError = true
                            resentSuccess = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(RadiusDefault),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary,
                        contentColor = ColorOnPrimary
                    )
                ) {
                    Text(
                        text = "CONTINUE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.96.sp
                    )
                }

                Spacer(Modifier.height(SpacingStackMd))

                // ── Resend button ────────────────────────────────────────
                OutlinedButton(
                    onClick = {
                        viewModel.resendVerificationEmail()
                        resentSuccess = true
                        notVerifiedError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(RadiusDefault),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ColorOutlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ColorOnSurface
                    )
                ) {
                    Text(
                        text = "RESEND EMAIL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.96.sp
                    )
                }

                Spacer(Modifier.height(SpacingStackSm))

                // ── Error from ViewModel (resend failure) ────────────────
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
                            modifier = Modifier.padding(top = SpacingStackSm)
                        )
                    }
                }
            }
        }
    }
}
