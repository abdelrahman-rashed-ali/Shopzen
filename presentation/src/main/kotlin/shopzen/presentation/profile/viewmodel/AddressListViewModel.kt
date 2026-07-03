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
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.profile.usecase.DeleteAddressUseCase
import shopzen.domain.profile.usecase.GetSavedAddressesUseCase
import shopzen.domain.profile.usecase.SetDefaultAddressUseCase
import shopzen.presentation.profile.intent.AddressListIntent
import shopzen.presentation.profile.state.AddressListState

@HiltViewModel
class AddressListViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val setDefaultAddressUseCase: SetDefaultAddressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddressListState())
    val state: StateFlow<AddressListState> = _state.asStateFlow()

    private var loadJob: Job? = null
    private var currentUid: String? = null

    init {
        loadAddresses()
    }

    fun processIntent(intent: AddressListIntent) {
        when (intent) {
            AddressListIntent.LoadAddresses -> loadAddresses()
            AddressListIntent.AddLocationClicked -> {
                if (_state.value.isGuest) {
                    _state.update { it.copy(showAuthRequiredDialog = true) }
                }
            }
            is AddressListIntent.RequestDeleteAddress -> _state.update {
                it.copy(showDeleteDialog = true, pendingDeleteAddressId = intent.addressId)
            }
            AddressListIntent.ConfirmDeleteAddress -> deletePendingAddress()
            is AddressListIntent.SetDefaultAddress -> setDefault(intent.addressId)
            AddressListIntent.DismissDialog -> _state.update {
                it.copy(
                    showAuthRequiredDialog = false,
                    showDeleteDialog = false,
                    pendingDeleteAddressId = null
                )
            }
        }
    }

    private fun loadAddresses() {
        loadJob?.cancel()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val user = getCurrentUserUseCase().getOrNull()
            if (user == null || user.email.isBlank()) {
                currentUid = null
                _state.update { it.copy(isLoading = false, isGuest = true, addresses = emptyList()) }
                return@launch
            }
            currentUid = user.uid
            _state.update { it.copy(isGuest = false) }
            loadJob = getSavedAddressesUseCase(user.uid)
                .onEach { result ->
                    result.fold(
                        onSuccess = { addresses ->
                            _state.update {
                                it.copy(isLoading = false, error = null, addresses = addresses)
                            }
                        },
                        onFailure = { throwable ->
                            _state.update {
                                it.copy(isLoading = false, error = throwable.message ?: "Could not load locations")
                            }
                        }
                    )
                }
                .launchIn(viewModelScope)
        }
    }

    private fun deletePendingAddress() {
        val uid = currentUid ?: return
        val addressId = _state.value.pendingDeleteAddressId ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteDialog = false) }
            deleteAddressUseCase(uid, addressId).fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, pendingDeleteAddressId = null) }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            pendingDeleteAddressId = null,
                            error = throwable.message ?: "Delete failed"
                        )
                    }
                }
            )
        }
    }

    private fun setDefault(addressId: String) {
        val uid = currentUid ?: return
        viewModelScope.launch {
            setDefaultAddressUseCase(uid, addressId).onFailure { throwable ->
                _state.update { it.copy(error = throwable.message ?: "Could not update default location") }
            }
        }
    }
}
