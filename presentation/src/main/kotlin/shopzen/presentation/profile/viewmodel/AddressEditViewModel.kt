package shopzen.presentation.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
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
import shopzen.domain.profile.model.Address
import shopzen.domain.profile.usecase.AddAddressUseCase
import shopzen.domain.profile.usecase.GetSavedAddressesUseCase
import shopzen.domain.profile.usecase.UpdateAddressUseCase
import shopzen.presentation.profile.intent.AddressEditIntent
import shopzen.presentation.profile.state.AddressEditState
import javax.inject.Inject

@HiltViewModel
class AddressEditViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val addAddressUseCase: AddAddressUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase,
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AddressEditState(addressId = savedStateHandle.get<String>("addressId")?.ifBlank { null }),
    )
    val state: StateFlow<AddressEditState> = _state.asStateFlow()

    init {
        loadAddressForEdit()
    }

    fun processIntent(intent: AddressEditIntent) {
        when (intent) {
            is AddressEditIntent.LabelChanged ->
                _state.update { it.copy(label = intent.value, error = null, isSaved = false) }

            is AddressEditIntent.RecipientNameChanged ->
                _state.update { it.copy(recipientName = intent.value, error = null, isSaved = false) }

            is AddressEditIntent.AddressLine1Changed ->
                _state.update { it.copy(addressLine1 = intent.value, error = null, isSaved = false) }

            is AddressEditIntent.AddressLine2Changed ->
                _state.update { it.copy(addressLine2 = intent.value, error = null, isSaved = false) }

            is AddressEditIntent.CityChanged ->
                _state.update { it.copy(city = intent.value, error = null, isSaved = false) }

            is AddressEditIntent.StateOrProvinceChanged ->
                _state.update { it.copy(stateOrProvince = intent.value, error = null, isSaved = false) }

            is AddressEditIntent.PostalCodeChanged ->
                _state.update { it.copy(postalCode = intent.value, error = null, isSaved = false) }

            is AddressEditIntent.CountryChanged ->
                _state.update { it.copy(country = intent.value, error = null, isSaved = false) }

            is AddressEditIntent.PhoneChanged ->
                _state.update { it.copy(phone = intent.value, error = null, isSaved = false) }

            AddressEditIntent.Submit -> submit()
        }
    }

    private fun loadAddressForEdit() {
        val addressId = _state.value.addressId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val uid = currentUidOrNull()
            if (uid == null) {
                _state.update { it.copy(isLoading = false, error = SIGN_IN_REQUIRED_ERROR) }
                return@launch
            }

            getSavedAddressesUseCase(uid).first().fold(
                onSuccess = { addresses ->
                    val existing = addresses.firstOrNull { it.id == addressId }
                    if (existing == null) {
                        _state.update { it.copy(isLoading = false, error = ADDRESS_NOT_FOUND_ERROR) }
                    } else {
                        _state.update { it.fromAddress(existing).copy(isLoading = false, error = null) }
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: ADDRESS_LOAD_ERROR,
                        )
                    }
                },
            )
        }
    }

    private fun submit() {
        val current = _state.value
        if (!current.hasRequiredFields()) {
            _state.update { it.copy(error = REQUIRED_FIELDS_ERROR) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val uid = currentUidOrNull()
            if (uid == null) {
                _state.update { it.copy(isLoading = false, error = SIGN_IN_REQUIRED_ERROR) }
                return@launch
            }

            val address = current.toAddress()
            val result = if (current.isEditMode) {
                updateAddressUseCase(uid, address)
            } else {
                addAddressUseCase(uid, address)
            }

            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: ADDRESS_SAVE_ERROR,
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

    private fun AddressEditState.hasRequiredFields(): Boolean =
        recipientName.isNotBlank() &&
            addressLine1.isNotBlank() &&
            city.isNotBlank() &&
            postalCode.isNotBlank() &&
            country.isNotBlank()

    private fun AddressEditState.toAddress(): Address =
        Address(
            id = addressId.orEmpty(),
            label = label.trim(),
            recipientName = recipientName.trim(),
            addressLine1 = addressLine1.trim(),
            addressLine2 = addressLine2.trim().ifBlank { null },
            city = city.trim(),
            stateOrProvince = stateOrProvince.trim().ifBlank { null },
            postalCode = postalCode.trim(),
            country = country.trim(),
            phone = phone.trim().ifBlank { null },
            isDefault = isDefault,
        )

    private fun AddressEditState.fromAddress(address: Address): AddressEditState =
        copy(
            label = address.label,
            recipientName = address.recipientName,
            addressLine1 = address.addressLine1,
            addressLine2 = address.addressLine2.orEmpty(),
            city = address.city,
            stateOrProvince = address.stateOrProvince.orEmpty(),
            postalCode = address.postalCode,
            country = address.country,
            phone = address.phone.orEmpty(),
            isDefault = address.isDefault,
        )

    private companion object {
        const val ADDRESS_LOAD_ERROR = "Unable to load address"
        const val ADDRESS_NOT_FOUND_ERROR = "Address not found"
        const val ADDRESS_SAVE_ERROR = "Unable to save address"
        const val REQUIRED_FIELDS_ERROR = "Please fill in all required fields"
        const val SIGN_IN_REQUIRED_ERROR = "Please sign in to manage addresses"
    }
}
