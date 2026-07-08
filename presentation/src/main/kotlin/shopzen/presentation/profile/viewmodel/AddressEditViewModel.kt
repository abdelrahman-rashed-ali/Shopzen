package shopzen.presentation.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import shopzen.domain.auth.usecase.GetCurrentUserUseCase
import shopzen.domain.profile.usecase.AddAddressUseCase
import shopzen.domain.profile.usecase.GetSavedAddressesUseCase
import shopzen.domain.profile.usecase.UpdateAddressUseCase
import shopzen.presentation.profile.intent.AddressEditIntent
import shopzen.presentation.profile.state.AddressEditState
import shopzen.presentation.profile.state.hasMissingRequiredFields
import shopzen.presentation.profile.state.mapPinSuggestion
import shopzen.presentation.profile.state.toDomainAddress
import shopzen.presentation.profile.state.toEditState
import shopzen.presentation.profile.state.withPlaceQuery
import shopzen.presentation.profile.state.withPlaceSuggestion
import shopzen.presentation.profile.state.withRecipientName

@HiltViewModel
class AddressEditViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSavedAddressesUseCase: GetSavedAddressesUseCase,
    private val addAddressUseCase: AddAddressUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddressEditState())
    val state: StateFlow<AddressEditState> = _state.asStateFlow()

    private val _events = kotlinx.coroutines.channels.Channel<AddressEditEvent>(kotlinx.coroutines.channels.Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentUid: String? = null
    
    private val searchEngine = com.mapbox.search.SearchEngine.createSearchEngineWithBuiltInDataProviders(
        com.mapbox.search.SearchEngineSettings()
    )
    private var searchTask: com.mapbox.search.common.AsyncOperationTask? = null
    private var cachedSuggestions: List<com.mapbox.search.result.SearchSuggestion> = emptyList()

    private val searchCallback = object : com.mapbox.search.SearchSuggestionsCallback {
        override fun onSuggestions(suggestions: List<com.mapbox.search.result.SearchSuggestion>, responseInfo: com.mapbox.search.ResponseInfo) {
            cachedSuggestions = suggestions
            val mapped = suggestions.map { s ->
                shopzen.presentation.profile.state.AddressPlaceSuggestion(
                    id = s.id,
                    title = s.name,
                    subtitle = s.descriptionText ?: s.address?.place ?: "",
                    addressLine1 = s.address?.street ?: "",
                    city = s.address?.place ?: "",
                    stateOrProvince = s.address?.region ?: "",
                    postalCode = s.address?.postcode ?: "",
                    country = s.address?.country ?: "",
                    latitude = 0.0,
                    longitude = 0.0,
                )
            }
            _state.update { it.copy(placeSuggestions = mapped) }
        }
        
        override fun onError(e: Exception) {
            _state.update { it.copy(error = e.message) }
        }
    }

    private val searchSelectionCallback = object : com.mapbox.search.SearchSelectionCallback {
        override fun onResult(
            suggestion: com.mapbox.search.result.SearchSuggestion,
            result: com.mapbox.search.result.SearchResult,
            responseInfo: com.mapbox.search.ResponseInfo
        ) {
            val mapped = shopzen.presentation.profile.state.AddressPlaceSuggestion(
                id = result.id,
                title = result.name,
                subtitle = result.descriptionText ?: "",
                addressLine1 = result.address?.street ?: "",
                city = result.address?.place ?: "",
                stateOrProvince = result.address?.region ?: "",
                postalCode = result.address?.postcode ?: "",
                country = result.address?.country ?: "",
                latitude = result.coordinate?.latitude() ?: 0.0,
                longitude = result.coordinate?.longitude() ?: 0.0,
            )
            _state.update { it.withPlaceSuggestion(mapped) }
        }

        override fun onSuggestions(suggestions: List<com.mapbox.search.result.SearchSuggestion>, responseInfo: com.mapbox.search.ResponseInfo) {}
        
        override fun onResults(suggestion: com.mapbox.search.result.SearchSuggestion, results: List<com.mapbox.search.result.SearchResult>, responseInfo: com.mapbox.search.ResponseInfo) {}

        override fun onError(e: Exception) {
            _state.update { it.copy(error = e.message) }
        }
    }

    fun processIntent(intent: AddressEditIntent) {
        when (intent) {
            is AddressEditIntent.LoadAddress -> loadAddress(intent.addressId)
            is AddressEditIntent.EntryModeChanged -> _state.update { it.copy(entryMode = intent.mode) }
            is AddressEditIntent.PlaceQueryChanged -> handlePlaceQueryChanged(intent.value)
            is AddressEditIntent.PlaceSuggestionSelected -> {
                _state.update { it.copy(placeQuery = intent.suggestion.title) }
                val originalSuggestion = cachedSuggestions.find { it.id == intent.suggestion.id }
                if (originalSuggestion != null) {
                    searchEngine.select(originalSuggestion, searchSelectionCallback)
                }
            }
            AddressEditIntent.UseMapPin -> {
                val lat = _state.value.selectedLatitude
                val lng = _state.value.selectedLongitude
                if (lat != null && lng != null) {
                    _state.update { it.copy(isLoading = true, error = null) }
                    searchEngine.search(
                        com.mapbox.search.ReverseGeoOptions(center = com.mapbox.geojson.Point.fromLngLat(lng, lat)),
                        object : com.mapbox.search.SearchCallback {
                            override fun onResults(results: List<com.mapbox.search.result.SearchResult>, responseInfo: com.mapbox.search.ResponseInfo) {
                                val result = results.firstOrNull()
                                if (result != null) {
                                    val mapped = shopzen.presentation.profile.state.AddressPlaceSuggestion(
                                        id = result.id,
                                        title = result.name,
                                        subtitle = result.descriptionText ?: "",
                                        addressLine1 = result.address?.street ?: result.name,
                                        city = result.address?.place ?: "",
                                        stateOrProvince = result.address?.region ?: "",
                                        postalCode = result.address?.postcode ?: "",
                                        country = result.address?.country ?: "",
                                        latitude = result.coordinate?.latitude() ?: lat,
                                        longitude = result.coordinate?.longitude() ?: lng,
                                    )
                                    _state.update { it.withPlaceSuggestion(mapped).copy(isLoading = false) }
                                } else {
                                    _state.update { it.withPlaceSuggestion(it.mapPinSuggestion()).copy(isLoading = false) }
                                }
                            }
                            override fun onError(e: Exception) {
                                _state.update { it.withPlaceSuggestion(it.mapPinSuggestion()).copy(isLoading = false, error = e.message) }
                            }
                        }
                    )
                }
            }
            is AddressEditIntent.MapPinMoved -> _state.update { it.copy(selectedLatitude = intent.latitude, selectedLongitude = intent.longitude) }
            is AddressEditIntent.LabelChanged -> _state.update { it.copy(label = intent.value, fieldError = null) }
            is AddressEditIntent.FirstNameChanged -> _state.update { it.copy(firstName = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.LastNameChanged -> _state.update { it.copy(lastName = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.RecipientNameChanged -> _state.update { it.withRecipientName(intent.value) }
            is AddressEditIntent.AddressLine1Changed -> _state.update { it.copy(addressLine1 = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.AddressLine2Changed -> _state.update { it.copy(addressLine2 = intent.value, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.CityChanged -> _state.update { it.copy(city = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.StateChanged -> _state.update { it.copy(stateOrProvince = intent.value, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.PostalCodeChanged -> _state.update { it.copy(postalCode = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.CountryChanged -> _state.update { it.copy(country = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            is AddressEditIntent.PhoneChanged -> _state.update { it.copy(phone = intent.value, fieldError = null, hasManualOverride = it.isAutofilled) }
            AddressEditIntent.OpenMapScreen -> _state.update { it.copy(isMapScreenOpen = true) }
            AddressEditIntent.CloseMapScreen -> _state.update { it.copy(isMapScreenOpen = false) }
            AddressEditIntent.Save -> save()
        }
    }
    
    private fun handlePlaceQueryChanged(query: String) {
        _state.update { it.withPlaceQuery(query) }
        searchTask?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(placeSuggestions = emptyList()) }
            return
        }
        searchTask = searchEngine.search(
            query = query,
            options = com.mapbox.search.SearchOptions(),
            callback = searchCallback
        )
    }

    private fun loadAddress(addressId: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, addressId = addressId) }
            val user = getCurrentUserUseCase().getOrNull()
            if (user == null || user.email.isBlank()) {
                currentUid = null
                _state.update { it.copy(isLoading = false, isGuest = true) }
                return@launch
            }
            currentUid = user.uid
            if (addressId == null) {
                _state.update { it.copy(isLoading = false, isGuest = false) }
                return@launch
            }
            val address = getSavedAddressesUseCase(user.uid).first().getOrNull()
                ?.firstOrNull { it.id == addressId }
            _state.update {
                if (address == null) {
                    it.copy(isLoading = false, isGuest = false, error = "Location not found")
                } else {
                    address.toEditState(it)
                }
            }
        }
    }

    private fun save() {
        val current = _state.value
        if (current.hasMissingRequiredFields()) {
            _state.update { it.copy(fieldError = "Please fill in all required fields") }
            return
        }
        val uid = currentUid ?: return
        val address = current.toDomainAddress()
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = if (current.addressId == null) {
                addAddressUseCase(uid, address)
            } else {
                updateAddressUseCase(uid, address)
            }
            result.fold(
                onSuccess = { 
                    _state.update { it.copy(isLoading = false, isSaved = true) }
                    _events.trySend(AddressEditEvent.NavigateBack)
                },
                onFailure = { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Save failed") }
                }
            )
        }
    }

}

sealed class AddressEditEvent {
    data object NavigateBack : AddressEditEvent()
}
