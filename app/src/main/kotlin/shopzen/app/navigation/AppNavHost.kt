package shopzen.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
import shopzen.presentation.onboarding.screen.OnboardingScreen
import shopzen.presentation.search.screen.SearchScreen as SearchRouteScreen
import shopzen.presentation.profile.screen.AddEditAddressScreen
import shopzen.presentation.profile.screen.OrderDetailScreen as OrderDetailRouteScreen
import shopzen.presentation.profile.screen.OrderHistoryScreen as OrderHistoryRouteScreen
import shopzen.presentation.profile.screen.PersonalDetailsScreen as PersonalDetailsRouteScreen
import shopzen.presentation.profile.screen.ProfileScreen as ProfileRouteScreen
import shopzen.presentation.profile.screen.SavedAddressesScreen
import shopzen.presentation.profile.screen.SettingsScreen as SettingsRouteScreen
import shopzen.presentation.cart.screen.CartScreen
import shopzen.presentation.checkout.screen.CheckoutSummaryScreen as CheckoutSummaryRouteScreen
import shopzen.presentation.checkout.screen.OrderConfirmationScreen as OrderConfirmationRouteScreen
import shopzen.presentation.checkout.screen.PaymentScreen as PaymentRouteScreen
import shopzen.presentation.category.screen.CategoryProductsScreen as CategoryProductsRouteScreen


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
    val mainNavigationViewModel: MainNavigationViewModel = hiltViewModel()

    fun navigateSingleTop(destination: NavScreen) {
        navController.navigate(destination) {
            launchSingleTop = true
        }
    }

    fun navigateTopLevel(destination: NavScreen) {
        navController.navigate(destination) {
            popUpTo<HomeScreen> {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateClearingBackStack(destination: NavScreen) {
        navController.navigate(destination) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateBackOrHome() {
        if (!navController.navigateUp()) {
            navigateClearingBackStack(HomeScreen)
        }
    }

    fun navigateToLogin() {
        navigateSingleTop(LoginScreen)
    }

    fun navigateToProduct(productId: String) {
        navController.navigate(ProductDetail(productId.toLongOrNull() ?: 0L)) {
            launchSingleTop = true
        }
    }

    fun navigateAuthRequired(destination: NavScreen) {
        mainNavigationViewModel.openAuthenticated(
            onAuthenticated = { navigateTopLevel(destination) },
            onGuest = { navigateToLogin() },
        )
    }

    fun navigateAuthRequiredSingle(destination: NavScreen) {
        mainNavigationViewModel.openAuthenticated(
            onAuthenticated = { navigateSingleTop(destination) },
            onGuest = { navigateToLogin() },
        )
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable<SplashScreen> {
            SplashRouteScreen(
                navigateToHome = {
                    navigateClearingBackStack(HomeScreen)
                },
                navigateToLogin = {
                    navigateClearingBackStack(LoginScreen)
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
                    navigateClearingBackStack(LoginScreen)
                }
            )
        }
        composable<LoginScreen> {
            LoginRouteScreen(
                navigateToHome = {
                    navigateClearingBackStack(HomeScreen)
                },
                navigateToRegister = {
                    navigateSingleTop(RegisterScreen)
                },
                navigateToForgotPassword = {
                    navigateSingleTop(ForgotPasswordScreen)
                },
                launchGoogleSignIn = launchGoogleSignIn,
                launchAppleSignIn = launchAppleSignIn
            )
        }
        composable<ForgotPasswordScreen> {
            LoginRouteScreen(
                navigateToHome = {
                    navigateClearingBackStack(HomeScreen)
                },
                navigateToRegister = {
                    navigateSingleTop(RegisterScreen)
                },
                navigateToForgotPassword = {
                    navigateBackOrHome()
                },
                launchGoogleSignIn = launchGoogleSignIn,
                launchAppleSignIn = launchAppleSignIn
            )
        }
        composable<RegisterScreen> {
            RegisterScreen(
                navigateToLogin = {
                    navController.navigate(LoginScreen) {
                        popUpTo<LoginScreen> {
                            inclusive = false
                        }
                        launchSingleTop = true
                    }
                },
                navigateToEmailVerification = {
                    navController.navigate(EmailVerificationScreen) {
                        popUpTo<RegisterScreen> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable<EmailVerificationScreen> {
            EmailVerificationScreen(
                navigateHome = {
                    navigateClearingBackStack(HomeScreen)
                }
            )
        }

        composable<HomeScreen> {
            HomeScreen(
                onNavigateToBrand = { brandName ->
                    navController.navigate(BrandProducts(brandName = brandName))
                },
                onNavigateToCategory = { categoryTitle ->
                    navController.navigate(CategoryProductsScreen(categoryTitle))
                },
                onNavigateToProduct = { productId ->
                    navigateToProduct(productId)
                },
                onNavigateToProducts = {
                    // TODO: Handle view all products
                },
                onNavigateToSearch = {
                    navigateTopLevel(SearchScreen)
                },
                onNavigateToProfile = {
                    navigateAuthRequired(ProfileScreen)
                },
                onNavigateToWishlist = {
                    navigateAuthRequired(WishlistScreen)
                },
                onNavigateToCart = {
                    navigateAuthRequired(CartScreen)
                },
                onNavigateToSettings = {
                    navigateTopLevel(SettingsScreen)
                },
                onNavigateToLogin = {
                    navController.navigate(LoginScreen)
                }
            )
        }

        composable<SearchScreen> {
            SearchRouteScreen(
                onNavigateToHome = {
                    navigateTopLevel(HomeScreen)
                },
                onNavigateToWishlist = {
                    navigateAuthRequired(WishlistScreen)
                },
                onNavigateToProduct = { productId ->
                    navigateToProduct(productId)
                },
                onNavigateToCategory = { categoryTitle ->
                    navController.navigate(CategoryProductsScreen(categoryTitle))
                },
                onNavigateToSearch = { navigateTopLevel(SearchScreen) },
                onNavigateToCart = {
                    navigateAuthRequired(CartScreen)
                },
                onNavigateToProfile = {
                    navigateAuthRequired(ProfileScreen)
                },
                onNavigateToSettings = {
                    navigateTopLevel(SettingsScreen)
                },
            )
        }

        composable<WishlistScreen> {
            shopzen.presentation.wishlist.screen.WishlistScreen(
                onNavigateToHome = {
                    navigateTopLevel(HomeScreen)
                },
                onNavigateToProduct = { productId ->
                    navigateToProduct(productId)
                },
                onNavigateToSearch = {
                    navigateTopLevel(SearchScreen)
                },
                onNavigateToCart = {
                    navigateAuthRequired(CartScreen)
                },
                onNavigateToProfile = {
                    navigateAuthRequired(ProfileScreen)
                },
                onNavigateToSettings = {
                    navigateTopLevel(SettingsScreen)
                },
            )
        }

        composable<SettingsScreen> {
            SettingsRouteScreen(
                onNavigateToDiscover = { navigateTopLevel(HomeScreen) },
                onNavigateToSearch = { navigateTopLevel(SearchScreen) },
                onNavigateToWishlist = { navigateAuthRequired(WishlistScreen) },
                onNavigateToProfile = { navigateAuthRequired(ProfileScreen) },
                onNavigateToCart = { navigateAuthRequired(CartScreen) },
                onNavigateToLogin = { navigateToLogin() },
                onNavigateToAddresses = { navigateAuthRequiredSingle(AddressesScreen) },
            )
        }

        composable<ProductDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<ProductDetail>()

            ProductDetailScreen(
                productId = args.productId,
                onNavigateBack = {
                    navigateBackOrHome()
                },
                onNavigateToLogin = {
                    navigateToLogin()
                }
            )
        }
        composable<CartScreen> {
            CartScreen(
                onNavigateBack = { navigateBackOrHome() },
                onNavigateToProduct = { productId -> navigateToProduct(productId) },
                onNavigateToCheckout = { navigateSingleTop(CheckoutSummaryScreen) },
                onNavigateToLogin = { navigateToLogin() },
            )
        }

        // ── Profile & Settings feature ──

        composable<CheckoutSummaryScreen> {
            CheckoutSummaryRouteScreen(
                onNavigateBack = { navigateBackOrHome() },
                onNavigateToLogin = { navigateToLogin() },
                onNavigateToAddAddress = {
                    navigateSingleTop(AddressEditScreen(addressId = null))
                },
                onNavigateToPayment = { navigateSingleTop(CheckoutPaymentScreen) },
            )
        }

        composable<CheckoutPaymentScreen> {
            PaymentRouteScreen(
                onNavigateBack = { navigateBackOrHome() },
                onNavigateToLogin = { navigateToLogin() },
                onNavigateToOrderConfirmation = { orderId, orderNumber ->
                    navController.navigate(
                        OrderConfirmationScreen(
                            orderId = orderId,
                            orderNumber = orderNumber,
                        )
                    ) {
                        popUpTo<CartScreen> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<OrderConfirmationScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<OrderConfirmationScreen>()
            OrderConfirmationRouteScreen(
                orderId = args.orderId,
                orderNumber = args.orderNumber,
                onNavigateBack = { navigateBackOrHome() },
                onContinueShopping = {
                    navigateClearingBackStack(HomeScreen)
                },
            )
        }

        composable<ProfileScreen> {
            ProfileRouteScreen(
                onNavigateToLogin = {
                    navigateToLogin()
                },
                onNavigateToPersonalDetails = { navigateSingleTop(PersonalDetailsScreen) },
                onNavigateToAddresses = { navigateSingleTop(AddressesScreen) },
                onNavigateToOrderHistory = { navigateSingleTop(OrderHistoryScreen) },
                onSignedOut = {
                    navigateClearingBackStack(LoginScreen)
                }
            )
        }

        composable<PersonalDetailsScreen> {
            PersonalDetailsRouteScreen(
                onNavigateBack = { navigateBackOrHome() }
            )
        }

        composable<OrderHistoryScreen> {
            OrderHistoryRouteScreen(
                onNavigateBack = { navigateBackOrHome() },
                onOrderClick = { orderId ->
                    navigateSingleTop(OrderDetailScreen(orderId = orderId))
                },
            )
        }

        composable<OrderDetailScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<OrderDetailScreen>()
            OrderDetailRouteScreen(
                orderId = args.orderId,
                onNavigateBack = { navigateBackOrHome() },
            )
        }

        composable<AddressesScreen> {
            SavedAddressesScreen(
                onNavigateBack = { navigateBackOrHome() },
                onNavigateToLogin = { navigateToLogin() },
                onNavigateToEdit = { addressId ->
                    navigateSingleTop(AddressEditScreen(addressId = addressId))
                }
            )
        }

        composable<AddressEditScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<AddressEditScreen>()

            AddEditAddressScreen(
                addressId = args.addressId,
                onNavigateBack = { navigateBackOrHome() }
            )
        }

        composable<BrandListScreen> {
            shopzen.presentation.brand.screen.BrandListScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToBrandProducts = { brandName ->
                    navController.navigate(BrandProducts(brandName = brandName))
                }
            )
        }

        composable<BrandProducts> { backStackEntry ->
            val args = backStackEntry.toRoute<BrandProducts>()

            shopzen.presentation.brand.screen.BrandProductsScreen(
                brandName = args.brandName,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onNavigateToProduct = { productId ->
                    navController.navigate(ProductDetail(productId.toLongOrNull() ?: 0L))
                }
            )
        }

        composable<CategoryProductsScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<CategoryProductsScreen>()

            CategoryProductsRouteScreen(
                categoryTitle = args.categoryTitle,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToProduct = { productId ->
                    navController.navigate(
                        ProductDetail(
                            productId.toLongOrNull() ?: 0L
                        )
                    )
                }
            )
        }
    }
}
