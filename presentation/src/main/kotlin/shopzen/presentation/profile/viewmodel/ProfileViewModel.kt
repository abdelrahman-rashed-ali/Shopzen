package shopzen.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.auth.model.User
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.auth.usecase.SignOutUseCase
import shopzen.domain.profile.model.UserProfile
import shopzen.domain.profile.usecase.GetUserPreferencesUseCase
import shopzen.domain.profile.usecase.GetUserProfileUseCase
import shopzen.domain.profile.usecase.SetCurrencyUseCase
import shopzen.domain.profile.usecase.SetLanguageUseCase
import shopzen.domain.profile.usecase.SetThemeUseCase
import shopzen.presentation.profile.intent.ProfileIntent
import shopzen.presentation.profile.state.ProfileNavigationTarget
import shopzen.presentation.profile.state.ProfileState

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val setCurrencyUseCase: SetCurrencyUseCase,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private var profileJob: Job? = null

    init {
        observePreferences()
        loadProfile()
    }

    fun processIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadProfile -> loadProfile()
            is ProfileIntent.ChangeCurrency -> viewModelScope.launch { setCurrencyUseCase(intent.currency) }
            is ProfileIntent.ChangeLanguage -> viewModelScope.launch { setLanguageUseCase(intent.language) }
            is ProfileIntent.ChangeTheme -> viewModelScope.launch { setThemeUseCase(intent.theme) }
            ProfileIntent.PersonalDetailsClicked -> openAuthenticated(ProfileNavigationTarget.PERSONAL_DETAILS)
            ProfileIntent.SavedLocationsClicked -> openAuthenticated(ProfileNavigationTarget.SAVED_LOCATIONS)
            ProfileIntent.RequestSignOut -> _state.update { it.copy(showLogoutDialog = true) }
            ProfileIntent.ConfirmSignOut -> signOut()
            ProfileIntent.DismissDialog -> _state.update {
                it.copy(showLogoutDialog = false, showAuthRequiredDialog = false)
            }
            ProfileIntent.NavigationHandled -> _state.update { it.copy(navigationTarget = null) }
        }
    }

    private fun observePreferences() {
        getUserPreferencesUseCase()
            .onEach { preferences -> _state.update { it.copy(preferences = preferences) } }
            .launchIn(viewModelScope)
    }

    private fun loadProfile() {
        profileJob?.cancel()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val user = getCurrentUserUseCase().getOrNull()
            if (user == null || user.email.isBlank()) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isGuest = true,
                        profile = user?.toProfile()
                    )
                }
                return@launch
            }
            _state.update {
                it.copy(
                    isGuest = false,
                    profile = user.toProfile()
                )
            }
            profileJob = getUserProfileUseCase(user.uid)
                .onEach { result ->
                    result.fold(
                        onSuccess = { profile ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = null,
                                    isGuest = false,
                                    profile = profile.withFallback(user)
                                )
                            }
                        },
                        onFailure = { throwable ->
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    error = null,
                                    isGuest = false,
                                    profile = user.toProfile()
                                )
                            }
                        }
                    )
                }
                .launchIn(viewModelScope)
        }
    }

    private fun openAuthenticated(target: ProfileNavigationTarget) {
        _state.update {
            if (it.isGuest) it.copy(showAuthRequiredDialog = true) else it.copy(navigationTarget = target)
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showLogoutDialog = false, error = null) }
            signOutUseCase().fold(
                onSuccess = {
                    _state.update {
                        it.copy(isLoading = false, isSignedOut = true, isGuest = true, profile = null)
                    }
                },
                onFailure = { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Sign out failed") }
                }
            )
        }
    }

    private fun User.toProfile(): UserProfile = UserProfile(
        uid = uid,
        fullName = displayName,
        email = email,
        photoUrl = photoUrl
    )

    private fun UserProfile.withFallback(user: User): UserProfile = copy(
        uid = uid.ifBlank { user.uid },
        fullName = fullName.ifBlank { user.displayName },
        email = email.ifBlank { user.email },
        photoUrl = photoUrl ?: user.photoUrl
    )
}
