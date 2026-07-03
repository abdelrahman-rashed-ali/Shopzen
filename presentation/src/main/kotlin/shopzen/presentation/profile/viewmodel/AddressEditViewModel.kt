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
import shopzen.domain.profile.model.Address
import shopzen.domain.profile.usecase.AddAddressUseCase
import shopzen.domain.profile.usecase.GetSavedAddressesUseCase
import shopzen.domain.profile.usecase.UpdateAddressUseCase
import shopzen.presentation.profile.intent.AddressEditIntent
import shopzen.presentation.profile.state.AddressEditState

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
            is AddressEditIntent.LabelChanged -> _state.update { it.copy(label = intent.value, fieldError = null) }
            is AddressEditIntent.RecipientNameChanged -> _state.update { it.copy(recipientName = intent.value, fieldError = null) }
            is AddressEditIntent.AddressLine1Changed -> _state.update { it.copy(addressLine1 = intent.value, fieldError = null) }
            is AddressEditIntent.AddressLine2Changed -> _state.update { it.copy(addressLine2 = intent.value) }
            is AddressEditIntent.CityChanged -> _state.update { it.copy(city = intent.value, fieldError = null) }
            is AddressEditIntent.StateChanged -> _state.update { it.copy(stateOrProvince = intent.value) }
            is AddressEditIntent.PostalCodeChanged -> _state.update { it.copy(postalCode = intent.value, fieldError = null) }
            is AddressEditIntent.CountryChanged -> _state.update { it.copy(country = intent.value, fieldError = null) }
            is AddressEditIntent.PhoneChanged -> _state.update { it.copy(phone = intent.value) }
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
                    it.copy(
                        isLoading = false,
                        isGuest = false,
                        label = address.label,
                        recipientName = address.recipientName,
                        addressLine1 = address.addressLine1,
                        addressLine2 = address.addressLine2.orEmpty(),
                        city = address.city,
                        stateOrProvince = address.stateOrProvince.orEmpty(),
                        postalCode = address.postalCode,
                        country = address.country,
                        phone = address.phone.orEmpty(),
                        isDefault = address.isDefault
                    )
                }
            }
        }
    }

    private fun save() {
        val current = _state.value
        if (
            current.recipientName.isBlank() ||
            current.addressLine1.isBlank() ||
            current.city.isBlank() ||
            current.postalCode.isBlank() ||
            current.country.isBlank()
        ) {
            _state.update { it.copy(fieldError = "Please fill in all required fields") }
            return
        }
        val uid = currentUid ?: return
        val address = Address(
            id = current.addressId.orEmpty(),
            label = current.label.trim(),
            recipientName = current.recipientName.trim(),
            addressLine1 = current.addressLine1.trim(),
            addressLine2 = current.addressLine2.trim().ifBlank { null },
            city = current.city.trim(),
            stateOrProvince = current.stateOrProvince.trim().ifBlank { null },
            postalCode = current.postalCode.trim(),
            country = current.country.trim(),
            phone = current.phone.trim().ifBlank { null },
            isDefault = current.isDefault
        )
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
