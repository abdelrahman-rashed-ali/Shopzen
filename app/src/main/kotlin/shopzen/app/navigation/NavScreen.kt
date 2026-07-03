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
object WishlistScreen : NavScreen(
    "wishlist"
)

@Serializable
data class ProductDetail(val productId: Long) : NavScreen("product-detail")

@Serializable
data object ProfileScreen : NavScreen("profile")

@Serializable
data object PersonalDetailsScreen : NavScreen("personal-details")

@Serializable
data object AddressesScreen : NavScreen("addresses")

@Serializable
data object SearchScreen : NavScreen("search")

@Serializable
data object CartScreen : NavScreen("cart")
@Serializable
data class AddressEditScreen(val addressId: String? = null) : NavScreen("address-edit")