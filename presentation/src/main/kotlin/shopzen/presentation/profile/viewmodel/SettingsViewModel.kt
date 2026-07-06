package shopzen.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.auth.usecase.SignOutUseCase
import shopzen.domain.profile.usecase.GetUserPreferencesUseCase
import shopzen.domain.profile.usecase.SetCurrencyUseCase
import shopzen.domain.profile.usecase.SetLanguageUseCase
import shopzen.domain.profile.usecase.SetThemeUseCase
import shopzen.presentation.profile.intent.SettingsIntent
import shopzen.presentation.profile.state.SettingsState

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observePreferences()
        loadSettings()
    }

    fun processIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.LoadSettings -> loadSettings()
            is SettingsIntent.ChangeCurrency -> savePreference {
                setCurrencyUseCase(intent.currency)
            }
            is SettingsIntent.ChangeLanguage -> savePreference {
                setLanguageUseCase(intent.language)
            }
            is SettingsIntent.ChangeTheme -> savePreference {
                setThemeUseCase(intent.theme)
            }
            SettingsIntent.RequestFirebaseSync -> requestFirebaseSync()
            SettingsIntent.RequestLogout -> _state.update { it.copy(showLogoutDialog = true) }
            SettingsIntent.ConfirmLogout -> signOut()
            SettingsIntent.DismissDialog -> _state.update {
                it.copy(showLogoutDialog = false, showAuthRequiredDialog = false)
            }
        }
    }

    private fun observePreferences() {
        getUserPreferencesUseCase()
            .onEach { preferences ->
                _state.update { it.copy(preferences = preferences) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val user = getCurrentUserUseCase().getOrNull()
            _state.update {
                it.copy(
                    isLoading = false,
                    isGuest = user?.email.isNullOrBlank(),
                )
            }
        }
    }

    private fun savePreference(save: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            save().onFailure { throwable ->
                _state.update { it.copy(error = throwable.message) }
            }
        }
    }

    private fun requestFirebaseSync() {
        _state.update {
            if (it.isGuest) {
                it.copy(showAuthRequiredDialog = true)
            } else {
                it.copy(error = null)
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showLogoutDialog = false, error = null) }
            signOutUseCase().fold(
                onSuccess = {
                    _state.update {
                        it.copy(isLoading = false, isGuest = true)
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(isLoading = false, error = throwable.message ?: "Sign out failed")
                    }
                },
            )
        }
    }
}
