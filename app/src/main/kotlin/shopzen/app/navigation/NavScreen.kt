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
object LoginScreen : NavScreen(
    "login"
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
data class ProductDetail(val productId: Long) : NavScreen("product-detail")

@Serializable
object CartScreen : NavScreen(
    "cart"
)
