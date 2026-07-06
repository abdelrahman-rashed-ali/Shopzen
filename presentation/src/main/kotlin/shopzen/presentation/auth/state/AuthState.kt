package shopzen.presentation.auth.state

import shopzen.domain.auth.model.User

data class AuthState(
    val isLoading: Boolean = true,
    val user: User? = null
)
