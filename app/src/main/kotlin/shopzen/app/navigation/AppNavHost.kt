package shopzen.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import shopzen.presentation.catalog.screen.HomeScreen
import shopzen.presentation.product.screen.ProductDetailScreen
import shopzen.presentation.auth.screen.EmailVerificationScreen
import shopzen.presentation.auth.screen.RegisterScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: NavScreen = HomeScreen, // Default to HomeScreen for catalog review
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
                    // Handle brand navigation
                },
                onNavigateToCategory = { categoryId ->
                    // Handle category navigation
                },
                onNavigateToProduct = { productId ->
                    navController.navigate(ProductDetail(productId.toLongOrNull() ?: 0L))
                },
                onNavigateToProducts = {
                    // Handle view all products
                }
            )
        }
        composable<ProductDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<ProductDetail>()
            ProductDetailScreen(productId = args.productId) {
                navController.navigateUp()
            }
        }
    }
}
