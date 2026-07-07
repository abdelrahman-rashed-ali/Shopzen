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
import shopzen.domain.profile.usecase.AddAddressUseCase
import shopzen.domain.profile.usecase.GetSavedAddressesUseCase
import shopzen.domain.profile.usecase.UpdateAddressUseCase
import shopzen.presentation.profile.intent.AddressEditIntent
import shopzen.presentation.profile.state.AddressEditState
import shopzen.presentation.profile.state.hasMissingRequiredFields
import shopzen.presentation.profile.state.mapPinSuggestion
import shopzen.presentation.profile.state.toDomainAddress
import shopzen.presentation.profile.state.toEditState
import shopzen.presentation.profile.state.withPlaceQuery
import shopzen.presentation.profile.state.withPlaceSuggestion
import shopzen.presentation.profile.state.withRecipientName

@HiltViewModel
class AddressEditViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase,
    private val addAddressUseCase: AddAddressUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddressEditState())
    val state: StateFlow<AddressEditState> = _state.asStateFlow()

    private var currentUid: String? = null

    fun processIntent(intent: AddressEditIntent) {
        when (intent) {
            is AddressEditIntent.LoadAddress -> loadAddress(intent.addressId)
            is AddressEditIntent.EntryModeChanged -> _state.update { it.copy(entryMode = intent.mode) }
            is AddressEditIntent.PlaceQueryChanged -> _state.update { it.withPlaceQuery(intent.value) }
            is AddressEditIntent.PlaceSuggestionSelected -> _state.update { it.withPlaceSuggestion(intent.suggestion) }
            AddressEditIntent.UseMapPin -> _state.update { it.withPlaceSuggestion(it.mapPinSuggestion()) }
            is AddressEditIntent.LabelChanged -> _state.update { it.copy(label = intent.value, fieldError = null) }
            is AddressEditIntent.FirstNameChanged -> _state.update { it.copy(firstName = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.LastNameChanged -> _state.update { it.copy(lastName = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.RecipientNameChanged -> _state.update { it.withRecipientName(intent.value) }
            is AddressEditIntent.AddressLine1Changed -> _state.update { it.copy(addressLine1 = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.AddressLine2Changed -> _state.update { it.copy(addressLine2 = intent.value, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.CityChanged -> _state.update { it.copy(city = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.StateChanged -> _state.update { it.copy(stateOrProvince = intent.value, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.PostalCodeChanged -> _state.update { it.copy(postalCode = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.CountryChanged -> _state.update { it.copy(country = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.PhoneChanged -> _state.update { it.copy(phone = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            AddressEditIntent.Save -> save()
        }
    }

    private fun loadAddress(addressId: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, addressId = addressId) }
            val user = getCurrentUserUseCase().getOrNull()
            if (user == null || user.email.isBlank()) {
                currentUid = null
                _state.update { it.copy(isLoading = false, isGuest = true) }
                return@launch
            }
            currentUid = user.uid
            if (addressId == null) {
                _state.update { it.copy(isLoading = false, isGuest = false) }
                return@launch
            }
            val address = getSavedAddressesUseCase(user.uid).first().getOrNull()
                ?.firstOrNull { it.id == addressId }
            _state.update {
                if (address == null) {
                    it.copy(isLoading = false, isGuest = false, error = "Location not found")
                } else {
                    address.toEditState(it)
                }
            }
        }
    }

    private fun save() {
        val current = _state.value
        if (current.hasMissingRequiredFields()) {
            _state.update { it.copy(fieldError = "Please fill in all required fields") }
            return
        }
        val uid = currentUid ?: return
        val address = current.toDomainAddress()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = if (current.addressId == null) {
                addAddressUseCase(uid, address)
            } else {
                updateAddressUseCase(uid, address)
            }
            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false, isSaved = true) } },
                onFailure = { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Save failed") }
                }
            )
        }
    }

}
