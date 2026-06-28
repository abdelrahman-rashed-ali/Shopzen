package com.iti.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shopzen.presentation.auth.screen.EmailVerificationScreen
import com.shopzen.presentation.auth.screen.RegisterScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: NavScreen = NavScreen.RegisterScreen,
) {


    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination.route

    ) {

        composable(
            NavScreen.SplashScreen.route
        ) {

        }

        composable(
            NavScreen.LoginScreen.route
        ) {

        }

        composable(
            NavScreen.RegisterScreen.route
        ) {
            RegisterScreen(
                navigateToLogin = {
                    navController.navigate(
                        NavScreen.LoginScreen.route
                    )
                },
                navigateToEmailVerification = {
                    navController.navigate(
                        NavScreen.EmailVerificationScreen.route
                    )
                }
            )
        }

        composable(
            NavScreen.EmailVerificationScreen.route
        ) {

            EmailVerificationScreen(
                navigateHome = {

                    navController.navigate(
                        NavScreen.HomeScreen.route
                    )
                }
            )
        }
        composable(
            NavScreen.HomeScreen.route
        ) {
            UnknownScreen(modifier)
        }
    }

}