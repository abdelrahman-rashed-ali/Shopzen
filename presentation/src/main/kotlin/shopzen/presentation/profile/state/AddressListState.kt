package shopzen.presentation.profile.state

import shopzen.domain.profile.model.Address

data class AddressListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGuest: Boolean = true,
    val addresses: List<Address> = emptyList(),
    val showAuthRequiredDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val pendingDeleteAddressId: String? = null
)
