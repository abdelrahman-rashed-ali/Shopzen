package shopzen.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import shopzen.presentation.auth.screen.LoginScreen as LoginRouteScreen
import shopzen.presentation.auth.screen.SplashScreen as SplashRouteScreen
import shopzen.presentation.product.screen.ProductDetailScreen
import shopzen.presentation.auth.screen.EmailVerificationScreen
import shopzen.presentation.auth.screen.RegisterScreen
import shopzen.presentation.catalog.screen.HomeScreen
import shopzen.presentation.product.screen.ProductDetailScreen
import shopzen.presentation.onboarding.screen.OnboardingScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: NavScreen = SplashScreen,
    launchGoogleSignIn: (
        onToken: (String) -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    launchAppleSignIn: (
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable<SplashScreen> {
            SplashRouteScreen(
                navigateToHome = {
                    navController.navigate(HomeScreen) {
                        popUpTo<SplashScreen> { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.navigate(LoginScreen) {
                        popUpTo<SplashScreen> { inclusive = true }
                    }
                },
                navigateToOnboarding = {
                    navController.navigate(shopzen.app.navigation.OnboardingScreen) {
                        popUpTo<SplashScreen> { inclusive = true }
                    }
                }
            )
        }
        composable<shopzen.app.navigation.OnboardingScreen> {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(LoginScreen) {
                        popUpTo<shopzen.app.navigation.OnboardingScreen> { inclusive = true }
                    }
                }
            )
        }
        composable<LoginScreen> {
            LoginRouteScreen(
                navigateToHome = {
                    navController.navigate(HomeScreen) {
                        popUpTo<LoginScreen> { inclusive = true }
                    }
                },
                navigateToRegister = {
                    navController.navigate(RegisterScreen)
                },
                navigateToForgotPassword = {
                    navController.navigate(ForgotPasswordScreen)
                },
                launchGoogleSignIn = launchGoogleSignIn,
                launchAppleSignIn = launchAppleSignIn
            )
        }
        composable<ForgotPasswordScreen> {
            LoginRouteScreen(
                navigateToHome = {
                    navController.navigate(HomeScreen) {
                        popUpTo<LoginScreen> { inclusive = true }
                    }
                },
                navigateToRegister = {
                    navController.navigate(RegisterScreen)
                },
                navigateToForgotPassword = {
                    navController.navigateUp()
                },
                launchGoogleSignIn = launchGoogleSignIn,
                launchAppleSignIn = launchAppleSignIn
            )
        }
        composable<RegisterScreen> {
            RegisterScreen(
                navigateToLogin = {
                    navController.navigate(LoginScreen)
                },
                navigateToEmailVerification = {
                    navController.navigate(EmailVerificationScreen)
                }
            )
        }

        composable<EmailVerificationScreen> {
            EmailVerificationScreen(
                navigateHome = {
                    navController.navigate(HomeScreen)
                }
            )
        }

        composable<HomeScreen> {
            HomeScreen(
                onNavigateToBrand = { brandName ->
                    // TODO: Handle brand navigation
                },
                onNavigateToCategory = { categoryId ->
                    // TODO: Handle category navigation
                },
                onNavigateToProduct = { productId ->
                    navController.navigate(
                        ProductDetail(
                            productId.toLongOrNull() ?: 0L
                        )
                    )
                },
                onNavigateToProducts = {
                    // TODO: Handle view all products
                }
            )
        }

        composable<ProductDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<ProductDetail>()

            ProductDetailScreen(
                productId = args.productId,
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}