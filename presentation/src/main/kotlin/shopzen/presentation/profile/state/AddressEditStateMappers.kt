package shopzen.presentation.profile.state

import shopzen.domain.profile.model.Address

/**
 * Update the query string and clear any open suggestion list.
 * Actual Search SDK call is debounced in AddressEditViewModel.
 */
internal fun AddressEditState.withPlaceQuery(value: String): AddressEditState =
    copy(
        placeQuery = value,
        placeSuggestions = if (value.isBlank()) emptyList() else placeSuggestions,
        fieldError = null,
    )

internal fun AddressEditState.withSuggestions(
    suggestions: List<AddressPlaceSuggestion>,
): AddressEditState = copy(placeSuggestions = suggestions)

internal fun AddressEditState.withPlaceSuggestion(suggestion: AddressPlaceSuggestion): AddressEditState =
    copy(
        selectedPlace = suggestion,
        selectedLatitude = suggestion.latitude,
        selectedLongitude = suggestion.longitude,
        placeQuery = suggestion.title,
        placeSuggestions = emptyList(),
        addressLine1 = suggestion.addressLine1,
        city = suggestion.city,
        stateOrProvince = suggestion.stateOrProvince,
        postalCode = suggestion.postalCode,
        country = suggestion.country,
        isAutofilled = true,
        hasManualOverride = false,
        fieldError = null,
    )

/** Apply a lat/lng from a map long-press (pin drop). */
internal fun AddressEditState.withMapPin(latitude: Double, longitude: Double): AddressEditState =
    copy(
        selectedLatitude = latitude,
        selectedLongitude = longitude,
        placeSuggestions = emptyList(),
        placeQuery = placeQuery,
        fieldError = null,
    )

internal fun AddressEditState.mapPinSuggestion(): AddressPlaceSuggestion =
    AddressPlaceSuggestion(
        id = "map-pin",
        title = "Pinned location",
        subtitle = "Mapbox map pick",
        addressLine1 = "Pinned map location",
        city = city.ifBlank { "Cairo" },
        stateOrProvince = stateOrProvince.ifBlank { "Cairo Governorate" },
        postalCode = postalCode.ifBlank { "11511" },
        country = country.ifBlank { "Egypt" },
        latitude = selectedLatitude ?: 30.0444,
        longitude = selectedLongitude ?: 31.2357,
    )

internal fun AddressEditState.withRecipientName(value: String): AddressEditState {
    val (first, last) = splitRecipientName(value)
    return copy(
        recipientName = value,
        firstName = first,
        lastName = last,
        fieldError = null,
        hasManualOverride = isAutofilled,
    )
}

internal fun AddressEditState.hasMissingRequiredFields(): Boolean =
    firstName.isBlank() ||
        lastName.isBlank() ||
        phone.isBlank() ||
        addressLine1.isBlank() ||
        city.isBlank() ||
        postalCode.isBlank() ||
        country.isBlank()

internal fun AddressEditState.toDomainAddress(): Address =
    Address(
        id = addressId.orEmpty(),
        label = label.trim(),
        recipientName = listOf(firstName, lastName).joinToString(" ").trim(),
        addressLine1 = addressLine1.trim(),
        addressLine2 = addressLine2.trim().ifBlank { null },
        city = city.trim(),
        stateOrProvince = stateOrProvince.trim().ifBlank { null },
        postalCode = postalCode.trim(),
        country = country.trim(),
        phone = phone.trim().ifBlank { null },
        isDefault = isDefault,
    )

internal fun Address.toEditState(current: AddressEditState): AddressEditState {
    val (first, last) = splitRecipientName(recipientName)
    return current.copy(
        isLoading = false,
        isGuest = false,
        label = label,
        firstName = first,
        lastName = last,
        recipientName = recipientName,
        addressLine1 = addressLine1,
        addressLine2 = addressLine2.orEmpty(),
        city = city,
        stateOrProvince = stateOrProvince.orEmpty(),
        postalCode = postalCode,
        country = country,
        phone = phone.orEmpty(),
        isDefault = isDefault,
    )
}

private fun splitRecipientName(value: String): Pair<String, String> {
    val parts = value.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return parts.firstOrNull().orEmpty() to parts.drop(1).joinToString(" ")
}
