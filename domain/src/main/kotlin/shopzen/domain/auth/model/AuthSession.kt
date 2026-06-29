package shopzen.domain.auth.model

data class AuthSession(
    val user: User?,
    val isGuest: Boolean
)
