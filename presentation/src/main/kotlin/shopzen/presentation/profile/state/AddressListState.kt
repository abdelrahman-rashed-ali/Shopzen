package shopzen.presentation.profile.state

import shopzen.domain.profile.model.Address

data class AddressListState(
    val addresses: List<Address> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val addressPendingDelete: Address? = null,
)
