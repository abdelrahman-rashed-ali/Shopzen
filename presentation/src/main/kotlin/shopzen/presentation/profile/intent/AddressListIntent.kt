package shopzen.presentation.profile.intent

import shopzen.domain.profile.model.Address

sealed interface AddressListIntent {
    data object LoadAddresses : AddressListIntent
    data class DeleteRequested(val address: Address) : AddressListIntent
    data object DeleteConfirmed : AddressListIntent
    data object DismissDeleteDialog : AddressListIntent
    data class SetDefaultRequested(val address: Address) : AddressListIntent
}
