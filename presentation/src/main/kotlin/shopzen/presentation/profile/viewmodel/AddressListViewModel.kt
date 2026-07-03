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
import shopzen.domain.profile.usecase.DeleteAddressUseCase
import shopzen.domain.profile.usecase.GetSavedAddressesUseCase
import shopzen.domain.profile.usecase.SetDefaultAddressUseCase
import shopzen.presentation.profile.intent.AddressListIntent
import shopzen.presentation.profile.state.AddressListState
import javax.inject.Inject

@HiltViewModel
class AddressListViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val setDefaultAddressUseCase: SetDefaultAddressUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AddressListState())
    val state: StateFlow<AddressListState> = _state.asStateFlow()

    private var addressesJob: Job? = null

    init {
        processIntent(AddressListIntent.LoadAddresses)
    }

    fun processIntent(intent: AddressListIntent) {
        when (intent) {
            AddressListIntent.LoadAddresses -> loadAddresses()
            is AddressListIntent.DeleteRequested ->
                _state.update { it.copy(addressPendingDelete = intent.address, error = null) }

            AddressListIntent.DeleteConfirmed -> confirmDelete()
            AddressListIntent.DismissDeleteDialog ->
                _state.update { it.copy(addressPendingDelete = null) }

            is AddressListIntent.SetDefaultRequested -> setDefault(intent.address.id)
        }
    }

    private fun loadAddresses() {
        addressesJob?.cancel()
        addressesJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val uid = currentUidOrNull()
            if (uid == null) {
                _state.update { it.copy(isLoading = false, error = SIGN_IN_REQUIRED_ERROR) }
                return@launch
            }

            getSavedAddressesUseCase(uid).collect { result ->
                result.fold(
                    onSuccess = { addresses ->
                        _state.update {
                            it.copy(
                                addresses = addresses,
                                isLoading = false,
                                error = null,
                            )
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
    }

    private fun confirmDelete() {
        val address = _state.value.addressPendingDelete ?: return
        viewModelScope.launch {
            val uid = currentUidOrNull()
            if (uid == null) {
                _state.update {
                    it.copy(
                        addressPendingDelete = null,
                        error = SIGN_IN_REQUIRED_ERROR,
                    )
                }
                return@launch
            }

            deleteAddressUseCase(uid, address.id).fold(
                onSuccess = {
                    _state.update { it.copy(addressPendingDelete = null, error = null) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            addressPendingDelete = null,
                            error = error.message ?: ADDRESS_DELETE_ERROR,
                        )
                    }
                },
            )
        }
    }

    private fun setDefault(addressId: String) {
        viewModelScope.launch {
            val uid = currentUidOrNull()
            if (uid == null) {
                _state.update { it.copy(error = SIGN_IN_REQUIRED_ERROR) }
                return@launch
            }

            setDefaultAddressUseCase(uid, addressId)
                .onFailure { error ->
                    _state.update { it.copy(error = error.message ?: ADDRESS_DEFAULT_ERROR) }
                }
        }
    }

    private suspend fun currentUidOrNull(): String? =
        getCurrentUserUseCase().getOrNull()
            ?.takeUnless { it.email.isBlank() }
            ?.uid

    private companion object {
        const val ADDRESS_LOAD_ERROR = "Unable to load addresses"
        const val ADDRESS_DELETE_ERROR = "Unable to delete address"
        const val ADDRESS_DEFAULT_ERROR = "Unable to set default address"
        const val SIGN_IN_REQUIRED_ERROR = "Please sign in to manage addresses"
    }
}
