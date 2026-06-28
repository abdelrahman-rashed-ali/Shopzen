package com.iti.myapplication.navigation


sealed class NavScreen(
    val route: String
) {
    object SplashScreen : NavScreen(
        "splash"
    )

    object LoginScreen : NavScreen(
        "auth/login"
    )

    object RegisterScreen : NavScreen(
        "auth/register"
    )

    object EmailVerificationScreen : NavScreen(
        "auth/verify-email"
    )

    object HomeScreen : NavScreen(
        "main/home"
    )

}