package shopzen.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.presentation.auth.intent.AuthIntent
import shopzen.presentation.auth.state.AuthState
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        processIntent(AuthIntent.CheckAuthStatus)
    }

    fun processIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.CheckAuthStatus -> checkAuthStatus()
        }
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = getCurrentUserUseCase()
            val user = result.getOrNull()
            _state.update {
                it.copy(
                    isLoading = false,
                    user = user
                )
            }
        }
    }
}
