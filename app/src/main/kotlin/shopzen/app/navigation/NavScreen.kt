package shopzen.app.navigation

import kotlinx.serialization.Serializable


@Serializable
sealed class NavScreen(
    val route: String
)

@Serializable
object SplashScreen : NavScreen(
    "splash"
)

@Serializable
object OnboardingScreen : NavScreen(
    "onboarding"
)

@Serializable
object LoginScreen : NavScreen(
    "login"
)

@Serializable
object ForgotPasswordScreen : NavScreen(
    "forgot-password"
)

@Serializable
object RegisterScreen : NavScreen(
    "register"
)

@Serializable
object EmailVerificationScreen : NavScreen(
    "verify-email"
)

@Serializable
object HomeScreen : NavScreen(
    "home"
)
@Serializable
data object WishlistScreen : NavScreen(
    "wishlist"
)

@Serializable
data class ProductDetail(val productId: Long) : NavScreen("product-detail")

@Serializable
data object CartScreen : NavScreen("cart")

@Serializable
data object CheckoutSummaryScreen : NavScreen("checkout-summary")

@Serializable
data object CheckoutPaymentScreen : NavScreen("checkout-payment")

@Serializable
data class OrderConfirmationScreen(
    val orderId: String,
    val orderNumber: String,
) : NavScreen("order-confirmation")

@Serializable
data object ProfileScreen : NavScreen("profile")

@Serializable
data object SettingsScreen : NavScreen("main/settings")

@Serializable
data object OrderHistoryScreen : NavScreen("main/profile/orders")

@Serializable
data class OrderDetailScreen(
    val orderId: String,
) : NavScreen("main/profile/orders/{orderId}")

@Serializable
data object PersonalDetailsScreen : NavScreen("personal-details")


@Serializable
data object AddressesScreen : NavScreen("addresses")

@Serializable
data object SearchScreen : NavScreen("search")

@Serializable
data class AddressEditScreen(val addressId: String? = null) : NavScreen("address-edit")

@Serializable
data object BrandListScreen : NavScreen("brand-list")

@Serializable
data class BrandProducts(val brandName: String) : NavScreen("brand-products")

@Serializable
data class CategoryProductsScreen(val categoryTitle: String) : NavScreen("category-products")
