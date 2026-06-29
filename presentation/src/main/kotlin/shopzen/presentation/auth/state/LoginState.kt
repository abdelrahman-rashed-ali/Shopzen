package shopzen.presentation.auth.state

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignedIn: Boolean = false,
    val isGuest: Boolean = false
)
