package com.iti.myapplication.navigation

import com.shopzen.app.navigation.Routes

sealed class NavScreen(val route: String) {
    object SplashScreen : NavScreen(Routes.SPLASH)
    object LoginScreen : NavScreen(Routes.LOGIN)
    object RegisterScreen : NavScreen(Routes.REGISTER)
    object EmailVerificationScreen : NavScreen(Routes.EMAIL_VERIFICATION)
    object HomeScreen : NavScreen(Routes.HOME)
}