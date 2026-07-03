package shopzen.presentation.profile.intent

sealed interface AddressEditIntent {
    data class LabelChanged(val value: String) : AddressEditIntent
    data class RecipientNameChanged(val value: String) : AddressEditIntent
    data class AddressLine1Changed(val value: String) : AddressEditIntent
    data class AddressLine2Changed(val value: String) : AddressEditIntent
    data class CityChanged(val value: String) : AddressEditIntent
    data class StateOrProvinceChanged(val value: String) : AddressEditIntent
    data class PostalCodeChanged(val value: String) : AddressEditIntent
    data class CountryChanged(val value: String) : AddressEditIntent
    data class PhoneChanged(val value: String) : AddressEditIntent
    data object Submit : AddressEditIntent
}
