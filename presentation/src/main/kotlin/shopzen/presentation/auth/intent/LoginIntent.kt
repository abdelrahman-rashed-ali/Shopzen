package shopzen.presentation.auth.intent

sealed class LoginIntent {
    data class EmailChanged(val email: String) : LoginIntent()
    data class PasswordChanged(val password: String) : LoginIntent()
    data object Submit : LoginIntent()
    data class SignInWithGoogle(val idToken: String) : LoginIntent()
    data class SignInWithApple(
        val idToken: String,
        val rawNonce: String
    ) : LoginIntent()
    data object ExternalSignInSucceeded : LoginIntent()
    data object ContinueAsGuest : LoginIntent()
    data class SocialSignInFailed(val message: String) : LoginIntent()
}
