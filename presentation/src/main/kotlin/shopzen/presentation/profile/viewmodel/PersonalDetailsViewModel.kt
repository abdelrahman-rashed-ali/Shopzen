package shopzen.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

@HiltViewModel
class PersonalDetailsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PersonalDetailsState())
    val state: StateFlow<PersonalDetailsState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun processIntent(intent: PersonalDetailsIntent) {
        when (intent) {
            is PersonalDetailsIntent.NameChanged ->
                _state.update { it.copy(fullName = intent.value, error = null, isSaved = false) }

            is PersonalDetailsIntent.PhoneChanged ->
                _state.update { it.copy(phone = intent.value, error = null, isSaved = false) }

            is PersonalDetailsIntent.PhotoChanged ->
                _state.update { it.copy(photoUrl = intent.value, error = null, isSaved = false) }

            PersonalDetailsIntent.Submit -> submit()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val uid = currentUidOrNull()
            if (uid == null) {
                _state.update { it.copy(isLoading = false, error = SIGN_IN_REQUIRED_ERROR) }
                return@launch
            }

            getUserProfileUseCase(uid).first().fold(
                onSuccess = { profile ->
                    _state.update {
                        it.copy(
                            fullName = profile.fullName,
                            email = profile.email,
                            phone = profile.phone,
                            photoUrl = profile.photoUrl,
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

    private fun submit() {
        val current = _state.value
        if (current.fullName.isBlank()) {
            _state.update { it.copy(error = NAME_REQUIRED_ERROR) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val uid = currentUidOrNull()
            if (uid == null) {
                _state.update { it.copy(isLoading = false, error = SIGN_IN_REQUIRED_ERROR) }
                return@launch
            }

            val profile = UserProfile(
                uid = uid,
                fullName = current.fullName.trim(),
                email = current.email.trim(),
                phone = current.phone.trim(),
                photoUrl = current.photoUrl?.trim()?.ifBlank { null },
            )

            updateUserProfileUseCase(profile).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: PROFILE_SAVE_ERROR,
                        )
                    }
                },
            )
        }
    }

    private suspend fun currentUidOrNull(): String? =
        getCurrentUserUseCase().getOrNull()
            ?.takeUnless { it.email.isBlank() }
            ?.uid

    private companion object {
        const val NAME_REQUIRED_ERROR = "Name is required"
        const val PROFILE_LOAD_ERROR = "Unable to load profile"
        const val PROFILE_SAVE_ERROR = "Unable to save profile"
        const val SIGN_IN_REQUIRED_ERROR = "Please sign in to manage your profile"
    }
}
