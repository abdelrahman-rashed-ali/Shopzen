package shopzen.presentation.profile.intent

sealed class AddressListIntent {
    data object LoadAddresses : AddressListIntent()
    data object AddLocationClicked : AddressListIntent()
    data class RequestDeleteAddress(val addressId: String) : AddressListIntent()
    data object ConfirmDeleteAddress : AddressListIntent()
    data class SetDefaultAddress(val addressId: String) : AddressListIntent()
    data object DismissDialog : AddressListIntent()
}
