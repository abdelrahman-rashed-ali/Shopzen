package shopzen.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import shopzen.domain.auth.usecase.SignInAsGuestUseCase
import shopzen.domain.auth.usecase.SignInWithAppleUseCase
import shopzen.domain.auth.usecase.SignInWithEmailUseCase
import shopzen.domain.auth.usecase.SignInWithGoogleUseCase
import shopzen.presentation.auth.intent.LoginIntent
import shopzen.presentation.auth.state.LoginState
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInWithAppleUseCase: SignInWithAppleUseCase,
    private val signInAsGuestUseCase: SignInAsGuestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged ->
                _state.update { it.copy(email = intent.email, error = null) }

            is LoginIntent.PasswordChanged ->
                _state.update { it.copy(password = intent.password, error = null) }

            LoginIntent.Submit -> signInWithEmail()
            is LoginIntent.SignInWithGoogle -> signInWithGoogle(intent.idToken)
            is LoginIntent.SignInWithApple -> signInWithApple(intent.idToken, intent.rawNonce)
            LoginIntent.ExternalSignInSucceeded ->
                _state.update { it.copy(isLoading = false, isSignedIn = true, error = null) }

            LoginIntent.ContinueAsGuest -> continueAsGuest()
            is LoginIntent.SocialSignInFailed ->
                _state.update { it.copy(error = intent.message, isLoading = false) }
        }
    }

    private fun signInWithEmail() {
        val current = _state.value
        if (current.email.isBlank() || current.password.isBlank()) {
            _state.update { it.copy(error = "All fields are required") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(current.email.trim()).matches()) {
            _state.update { it.copy(error = "Invalid email address") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        signInWithEmailUseCase(
            email = current.email.trim(),
            password = current.password
        ).onEach { result ->
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isSignedIn = true) }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Sign in failed. Please try again."
                        )
                    }
                }
            )
        }.launchIn(viewModelScope)
    }

    private fun signInWithGoogle(idToken: String) {
        _state.update { it.copy(isLoading = true, error = null) }
        signInWithGoogleUseCase(idToken).onEach { result ->
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isSignedIn = true) }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Google sign in failed. Please try again."
                        )
                    }
                }
            )
        }.launchIn(viewModelScope)
    }

    private fun signInWithApple(
        idToken: String,
        rawNonce: String
    ) {
        _state.update { it.copy(isLoading = true, error = null) }
        signInWithAppleUseCase(idToken, rawNonce).onEach { result ->
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isSignedIn = true) }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Apple sign in failed. Please try again."
                        )
                    }
                }
            )
        }.launchIn(viewModelScope)
    }

    private fun continueAsGuest() {
        _state.update { it.copy(isLoading = true, error = null) }
        signInAsGuestUseCase().onEach { result ->
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isGuest = true) }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Guest sign in failed. Please try again."
                        )
                    }
                }
            )
        }.launchIn(viewModelScope)
    }
}
