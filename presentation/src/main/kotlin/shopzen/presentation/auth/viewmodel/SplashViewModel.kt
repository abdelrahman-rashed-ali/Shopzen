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
import shopzen.presentation.auth.intent.SplashIntent
import shopzen.presentation.auth.state.SplashState
import shopzen.domain.onboarding.usecase.HasCompletedOnboardingUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val hasCompletedOnboardingUseCase: HasCompletedOnboardingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        processIntent(SplashIntent.CheckSession)
    }

    fun processIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.CheckSession -> checkSession()
        }
    }

    private fun checkSession() {
        viewModelScope.launch {
            val user = getCurrentUserUseCase().getOrNull()
            val hasCompletedOnboarding = hasCompletedOnboardingUseCase().first()
            _state.update {
                it.copy(
                    isLoading = false,
                    shouldNavigateHome = hasCompletedOnboarding && user != null,
                    shouldNavigateLogin = hasCompletedOnboarding && user == null,
                    shouldNavigateOnboarding = !hasCompletedOnboarding
                )
            }
        }
    }
}
