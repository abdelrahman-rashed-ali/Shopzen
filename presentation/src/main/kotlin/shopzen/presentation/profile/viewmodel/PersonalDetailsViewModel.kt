package shopzen.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.profile.model.UserProfile
import shopzen.domain.profile.usecase.GetUserProfileUseCase
import shopzen.domain.profile.usecase.UpdateUserProfileUseCase
import shopzen.presentation.profile.intent.PersonalDetailsIntent
import shopzen.presentation.profile.state.PersonalDetailsState

@HiltViewModel
class PersonalDetailsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PersonalDetailsState())
    val state: StateFlow<PersonalDetailsState> = _state.asStateFlow()

    init {
        loadDetails()
    }

    fun processIntent(intent: PersonalDetailsIntent) {
        when (intent) {
            PersonalDetailsIntent.LoadDetails -> loadDetails()
            is PersonalDetailsIntent.FullNameChanged -> _state.update {
                it.copy(fullName = intent.value, nameError = null, isSaved = false)
            }
            is PersonalDetailsIntent.PhoneChanged -> _state.update {
                it.copy(phone = intent.value, isSaved = false)
            }
            PersonalDetailsIntent.Save -> save()
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val user = getCurrentUserUseCase().getOrNull()
            if (user == null || user.email.isBlank()) {
                _state.update { it.copy(isLoading = false, isGuest = true) }
                return@launch
            }
            val fallback = PersonalDetailsState(
                isLoading = false,
                isGuest = false,
                uid = user.uid,
                fullName = user.displayName,
                email = user.email,
                photoUrl = user.photoUrl
            )
            val profile = getUserProfileUseCase(user.uid).first().getOrNull()
            _state.value = profile?.let {
                fallback.copy(
                    fullName = it.fullName.ifBlank { fallback.fullName },
                    email = it.email.ifBlank { fallback.email },
                    phone = it.phone,
                    photoUrl = it.photoUrl ?: fallback.photoUrl
                )
            } ?: fallback
        }
    }

    private fun save() {
        val current = _state.value
        if (current.fullName.isBlank()) {
            _state.update { it.copy(nameError = "Name is required") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val profile = UserProfile(
                uid = current.uid,
                fullName = current.fullName.trim(),
                email = current.email,
                phone = current.phone.trim(),
                photoUrl = current.photoUrl
            )
            updateUserProfileUseCase(profile).fold(
                onSuccess = { _state.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Save failed") }
                }
            )
        }
    }
}
