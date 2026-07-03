package shopzen.presentation.auth.intent

sealed class AuthIntent {
    data object CheckAuthStatus : AuthIntent()
}
