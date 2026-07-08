package shopzen.presentation.profile.state

data class AddressEditState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGuest: Boolean = true,
    val addressId: String? = null,
    val entryMode: AddressEntryMode = AddressEntryMode.ASSISTED,
    val placeQuery: String = "",
    val placeSuggestions: List<AddressPlaceSuggestion> = emptyList(),
    val selectedPlace: AddressPlaceSuggestion? = null,
    val selectedLatitude: Double? = null,
    val selectedLongitude: Double? = null,
    val isAutofilled: Boolean = false,
    val hasManualOverride: Boolean = false,
    val label: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val recipientName: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val city: String = "",
    val stateOrProvince: String = "",
    val postalCode: String = "",
    val country: String = "",
    val phone: String = "",
    val isDefault: Boolean = false,
    val fieldError: String? = null,
    val isSaved: Boolean = false,
    val isMapScreenOpen: Boolean = false
)

enum class AddressEntryMode {
    ASSISTED,
    MANUAL,
}

data class AddressPlaceSuggestion(
    val id: String,
    val title: String,
    val subtitle: String,
    val addressLine1: String,
    val city: String,
    val stateOrProvince: String,
    val postalCode: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
)
