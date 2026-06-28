package com.iti.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import com.shopzen.presentation.auth.screen.EmailVerificationScreen
import com.shopzen.presentation.auth.screen.RegisterScreen
import com.shopzen.app.navigation.Routes


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

        }


        composable(Routes.LOGIN) {

        }


        composable(Routes.REGISTER) {

            RegisterScreen(
                navigateToLogin = {
                    navController.navigate(
                        Routes.HOME
                    )
                },
                navigateToEmailVerification = {

                    navController.navigate(
                        Routes.EMAIL_VERIFICATION
                    )

                }
            )

        }


        composable(Routes.EMAIL_VERIFICATION) {

            EmailVerificationScreen(
                navigateHome = {

                    navController.navigate(
                        Routes.HOME
                    )

                }
            )

        }


        composable(Routes.HOME) {

            UnknownScreen(modifier)

        }
    }
}