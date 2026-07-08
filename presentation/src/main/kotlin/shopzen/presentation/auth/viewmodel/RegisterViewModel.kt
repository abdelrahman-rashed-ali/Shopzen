package shopzen.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import shopzen.domain.auth.usecase.RegisterWithEmailUseCase
import shopzen.domain.auth.usecase.SendVerificationEmailUseCase
import shopzen.domain.auth.usecase.CheckEmailVerifiedUseCase
import shopzen.presentation.auth.intent.RegisterIntent
import shopzen.presentation.auth.state.RegisterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val sendVerificationEmailUseCase: SendVerificationEmailUseCase,
    private val checkEmailVerifiedUseCase: CheckEmailVerifiedUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun processIntent(intent: RegisterIntent) {
        when (intent) {
            is RegisterIntent.NameChanged ->
                _state.update { it.copy(name = intent.name, error = null) }

            is RegisterIntent.EmailChanged ->
                _state.update { it.copy(email = intent.email, error = null) }

            is RegisterIntent.PasswordChanged ->
                _state.update { it.copy(password = intent.password, error = null) }

            is RegisterIntent.ConfirmPasswordChanged ->
                _state.update { it.copy(confirmPassword = intent.confirmPassword, error = null) }

            is RegisterIntent.Submit -> handleSubmit()
        }
    }

    private fun handleSubmit() {
        val current = _state.value
        if (current.name.isBlank() || current.email.isBlank() ||
            current.password.isBlank() || current.confirmPassword.isBlank()
        ) {
            _state.update { it.copy(error = "All fields are required") }
            return
        }
        if (current.password != current.confirmPassword) {
            _state.update { it.copy(error = "Passwords do not match") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        registerWithEmailUseCase(
            email = current.email.trim(),
            password = current.password.trim(),
            displayName = current.name.trim()
        ).onEach { result ->
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRegistered = true
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message
                                ?: "Registration failed. Please try again."
                        )
                    }
                }
            )
        }.launchIn(viewModelScope)
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            sendVerificationEmailUseCase()
        }
    }

    fun checkEmailVerified() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val isVerified = checkEmailVerifiedUseCase()
            _state.update {
                it.copy(
                    isLoading = false,
                    isEmailVerified = isVerified,
                    error = if (!isVerified) "Email not verified yet" else null
                )
            }
        }
    }
}
