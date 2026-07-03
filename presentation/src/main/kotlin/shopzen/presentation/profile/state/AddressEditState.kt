package shopzen.presentation.profile.state

data class AddressEditState(
    val addressId: String? = null,
    val label: String = "",
    val recipientName: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val stateOrProvince: String = "",
    val postalCode: String = "",
    val country: String = "",
    val phone: String = "",
    val isDefault: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
) {
    val isEditMode: Boolean get() = addressId != null
}
