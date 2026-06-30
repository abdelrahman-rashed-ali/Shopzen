package shopzen.presentation.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.onboarding.usecase.CompleteOnboardingUseCase
import shopzen.presentation.onboarding.intent.OnboardingIntent
import shopzen.presentation.onboarding.state.OnboardingState
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun processIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.CompleteOnboarding -> completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            completeOnboardingUseCase()
            _state.update { it.copy(isComplete = true) }
        }
    }
}
