package com.iti.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.iti.myapplication.ui.product.screen.ProductDetailScreen
import com.shopzen.presentation.auth.screen.EmailVerificationScreen
import com.shopzen.presentation.auth.screen.RegisterScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: NavScreen = RegisterScreen,
) {


    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination

    ) {
        composable<SplashScreen> {}
        composable<LoginScreen> {}
        composable<RegisterScreen> {
            RegisterScreen(
                navigateToLogin = {
                navController.navigate(
                    LoginScreen
                )
            },
                navigateToEmailVerification = { navController.navigate(EmailVerificationScreen) })
        }
        composable<EmailVerificationScreen> {
            EmailVerificationScreen(navigateHome = {
                navController.navigate(
                    HomeScreen
                )
            })
        }
        composable<HomeScreen> {}
        composable<ProductDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<ProductDetail>()

            ProductDetailScreen(productId = args.productId) { navController.navigateUp() }
        }
    }

}