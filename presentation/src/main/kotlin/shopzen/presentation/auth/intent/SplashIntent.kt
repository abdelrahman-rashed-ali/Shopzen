package shopzen.presentation.auth.intent

sealed class SplashIntent {
    data object CheckSession : SplashIntent()
}
