package shopzen.presentation.profile.intent

import shopzen.presentation.profile.state.AddressEntryMode
import shopzen.presentation.profile.state.AddressPlaceSuggestion

sealed class AddressEditIntent {
    data class LoadAddress(val addressId: String?) : AddressEditIntent()
    data class EntryModeChanged(val mode: AddressEntryMode) : AddressEditIntent()
    data class PlaceQueryChanged(val value: String) : AddressEditIntent()
    data class PlaceSuggestionSelected(val suggestion: AddressPlaceSuggestion) : AddressEditIntent()
    data object UseMapPin : AddressEditIntent()
    data class LabelChanged(val value: String) : AddressEditIntent()
    data class FirstNameChanged(val value: String) : AddressEditIntent()
    data class LastNameChanged(val value: String) : AddressEditIntent()
    data class RecipientNameChanged(val value: String) : AddressEditIntent()
    data class AddressLine1Changed(val value: String) : AddressEditIntent()
    data class AddressLine2Changed(val value: String) : AddressEditIntent()
    data class CityChanged(val value: String) : AddressEditIntent()
    data class StateChanged(val value: String) : AddressEditIntent()
    data class PostalCodeChanged(val value: String) : AddressEditIntent()
    data class CountryChanged(val value: String) : AddressEditIntent()
    data class PhoneChanged(val value: String) : AddressEditIntent()
    data object Save : AddressEditIntent()
}
