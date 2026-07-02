package shopzen.presentation.auth.state

data class SplashState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val shouldNavigateHome: Boolean = false,
    val shouldNavigateLogin: Boolean = false,
    val shouldNavigateOnboarding: Boolean = false
)
