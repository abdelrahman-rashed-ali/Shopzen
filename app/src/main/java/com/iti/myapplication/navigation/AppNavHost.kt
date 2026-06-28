package com.iti.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.shopzen.presentation.auth.screen.EmailVerificationScreen
import com.shopzen.presentation.auth.screen.RegisterScreen
import com.shopzen.presentation.navigation.Routes

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: NavScreen = NavScreen.RegisterScreen,
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination.route,
    ) {
        composable(Routes.SPLASH) {
            // SplashScreen(navController)
        }

        composable(Routes.LOGIN) {
            // LoginScreen(navController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController = navController)
        }

        composable(Routes.EMAIL_VERIFICATION) {
            EmailVerificationScreen(navController = navController)
        }

        composable(Routes.HOME) {
            UnknownScreen(modifier)
        }
    }
}