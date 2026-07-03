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
import shopzen.presentation.search.screen.SearchScreen as SearchRouteScreen
import shopzen.presentation.profile.screen.AddEditAddressScreen
import shopzen.presentation.profile.screen.PersonalDetailsScreen as PersonalDetailsRouteScreen
import shopzen.presentation.profile.screen.ProfileScreen as ProfileRouteScreen
import shopzen.presentation.profile.screen.SavedAddressesScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: NavScreen = HomeScreen,
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
                },
                onNavigateToSearch = {
                    navController.navigate(SearchScreen)
                },
                onNavigateToProfile = {
                    navController.navigate(ProfileScreen) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToWishlist = {
                    navController.navigate(WishlistScreen)
                },
                onNavigateToCart = {
                    navController.navigate(CartScreen)
                }
            )
        }

        composable<SearchScreen> {
            SearchRouteScreen(
                onNavigateToHome = {
                    navController.navigate(HomeScreen) {
                        popUpTo<HomeScreen> { inclusive = false }
                    }
                },
                onNavigateToWishlist = {
                    navController.navigate(WishlistScreen)
                },
                onNavigateToProduct = { productId ->
                    navController.navigate(
                        ProductDetail(
                            productId.toLongOrNull() ?: 0L
                        )
                    )
                },
                onNavigateToCategory = { categoryId ->
                    // TODO: Handle category navigation
                },
                onNavigateToSearch = {},
                onNavigateToCart = {
                    navController.navigate(CartScreen)
                },
                onNavigateToProfile = {
                    navController.navigate(ProfileScreen) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable<WishlistScreen> {
            shopzen.presentation.wishlist.screen.WishlistScreen(
                onNavigateToHome = {
                    navController.navigate(HomeScreen) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProduct = { productId ->
                    navController.navigate(
                        ProductDetail(productId.toLongOrNull() ?: 0L)
                    )
                },
                onNavigateToSearch = {

                },
                onNavigateToCart = {

                },
                onNavigateToProfile = {
                    navController.navigate(ProfileScreen) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        composable<ProductDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<ProductDetail>()

            ProductDetailScreen(
                productId = args.productId,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // ── Profile & Settings feature ──

        composable<ProfileScreen> {
            ProfileRouteScreen(
                onNavigateToHome = {
                    navController.navigate(HomeScreen) {
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(LoginScreen)
                },
                onNavigateToPersonalDetails = { navController.navigate(PersonalDetailsScreen) },
                onNavigateToAddresses = { navController.navigate(AddressesScreen) },
                onNavigateToSearch = {},
                onNavigateToCart = {},
                onNavigateToWishlist = {
                    navController.navigate(WishlistScreen)
                },
                onSignedOut = {
                    navController.navigate(LoginScreen) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }

        composable<PersonalDetailsScreen> {
            PersonalDetailsRouteScreen(
                onNavigateBack = { navController.navigateUp() }
            )
        }

        composable<AddressesScreen> {
            SavedAddressesScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToLogin = { navController.navigate(LoginScreen) },
                onNavigateToEdit = { addressId ->
                    navController.navigate(AddressEditScreen(addressId = addressId))
                }
            )
        }

        composable<AddressEditScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<AddressEditScreen>()

            AddEditAddressScreen(
                addressId = args.addressId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}