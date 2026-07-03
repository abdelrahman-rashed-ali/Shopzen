package shopzen.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.profile.usecase.GetUserPreferencesUseCase
import shopzen.domain.profile.usecase.GetUserProfileUseCase
import shopzen.domain.profile.usecase.SetCurrencyUseCase
import shopzen.domain.profile.usecase.SetLanguageUseCase
import shopzen.domain.profile.usecase.SetThemeUseCase
import shopzen.presentation.profile.intent.ProfileIntent
import shopzen.presentation.profile.state.ProfileNavigationTarget
import shopzen.presentation.profile.state.ProfileState
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _signedOut = MutableStateFlow(false)
    val signedOut: StateFlow<Boolean> = _signedOut.asStateFlow()

    private var profileJob: Job? = null
    private var preferencesJob: Job? = null

    init {
        processIntent(ProfileIntent.LoadProfile)
    }

    fun processIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.LanguageSelected -> setLanguage(intent.language)
            is ProfileIntent.ThemeSelected -> setTheme(intent.theme)
            is ProfileIntent.CurrencySelected -> setCurrency(intent.currency)
            ProfileIntent.PersonalDetailsClicked -> navigateOrRequireAuth(ProfileNavigationTarget.PERSONAL_DETAILS)
            ProfileIntent.SavedAddressesClicked -> navigateOrRequireAuth(ProfileNavigationTarget.SAVED_ADDRESSES)
            ProfileIntent.NavigationHandled -> _state.update { it.copy(navigationTarget = null) }
            ProfileIntent.DismissAuthRequiredDialog -> _state.update { it.copy(showAuthRequiredDialog = false) }
            ProfileIntent.SignOutClicked -> _state.update { it.copy(showLogoutDialog = true) }
            ProfileIntent.DismissLogoutDialog -> _state.update { it.copy(showLogoutDialog = false) }
            ProfileIntent.ConfirmSignOut -> confirmSignOut()
        }
    }

    private fun loadProfile() {
        observePreferences()
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentUser = getCurrentUserUseCase().getOrNull()
            val isGuest = currentUser == null || currentUser.email.isBlank()
            if (isGuest) {
                _state.update {
                    it.copy(
                        profile = null,
                        isGuest = true,
                        isLoading = false,
                    )
                }
                return@launch
            }

            getUserProfileUseCase(currentUser.uid).collect { result ->
                result.fold(
                    onSuccess = { profile ->
                        _state.update {
                            it.copy(
                                profile = profile,
                                isGuest = false,
                                isLoading = false,
                                error = null,
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: PROFILE_LOAD_ERROR,
                            )
                        }
                    },
                )
            }
        }
    }

    private fun observePreferences() {
        if (preferencesJob?.isActive == true) return
        preferencesJob = viewModelScope.launch {
            getUserPreferencesUseCase().collect { preferences ->
                _state.update { it.copy(preferences = preferences) }
            }
        }
    }

    private fun setLanguage(language: shopzen.domain.profile.model.AppLanguage) {
        viewModelScope.launch {
            setLanguageUseCase(language)
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    private fun setTheme(theme: shopzen.domain.profile.model.AppTheme) {
        viewModelScope.launch {
            setThemeUseCase(theme)
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    private fun setCurrency(currency: shopzen.domain.profile.model.AppCurrency) {
        viewModelScope.launch {
            setCurrencyUseCase(currency)
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    private fun navigateOrRequireAuth(target: ProfileNavigationTarget) {
        _state.update {
            if (it.isGuest) {
                it.copy(showAuthRequiredDialog = true)
            } else {
                it.copy(navigationTarget = target)
            }
        }
    }

    private fun confirmSignOut() {
        _state.update { it.copy(showLogoutDialog = false) }
        _signedOut.value = true
    }

    private companion object {
        const val PROFILE_LOAD_ERROR = "Unable to load profile"
    }
}
