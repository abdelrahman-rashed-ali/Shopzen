package shopzen.presentation.profile.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.CircleAnnotation
import shopzen.presentation.R
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.profile.intent.AddressEditIntent
import shopzen.presentation.profile.state.AddressEditState
import shopzen.presentation.profile.state.AddressEntryMode
import shopzen.presentation.profile.state.AddressPlaceSuggestion
import shopzen.presentation.profile.viewmodel.AddressEditEvent
import shopzen.presentation.profile.viewmodel.AddressEditViewModel


@Composable
fun AddEditAddressScreen(
    addressId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddressEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Collect one-shot NavigateBack events — Channel(BUFFERED) so no event is lost.
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AddressEditEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    LaunchedEffect(addressId) {
        viewModel.processIntent(AddressEditIntent.LoadAddress(addressId))
    }

    AddressEditContent(
        state = state,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
    
    AnimatedVisibility(
        visible = state.isMapScreenOpen,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        FullScreenMapScreen(
            state = state,
            onIntent = viewModel::processIntent,
            onClose = { viewModel.processIntent(AddressEditIntent.CloseMapScreen) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddressEditContent(
    state: AddressEditState,
    onIntent: (AddressEditIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (state.addressId == null) {
                                R.string.address_edit_title_add
                            } else {
                                R.string.address_edit_title_edit
                            },
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            if (!state.isLoading) {
                AddressSaveBar(
                    isSaving = state.isLoading,
                    onSave = { onIntent(AddressEditIntent.Save) },
                )
            }
        },
    ) { innerPadding ->
        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .testTag(AddressEditTestTags.Content),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                AddressEntryModeSwitcher(
                    selectedMode = state.entryMode,
                    onModeSelected = { onIntent(AddressEditIntent.EntryModeChanged(it)) },
                )
                AnimatedContent(
                    targetState = state.entryMode,
                    contentKey = { it.name },
                    transitionSpec = {
                        (fadeIn(tween(160)) + slideInVertically { it / 10 })
                            .togetherWith(fadeOut(tween(120)) + slideOutVertically { -it / 12 })
                    },
                    label = "address-entry-mode",
                ) { mode ->
                    if (mode == AddressEntryMode.ASSISTED) {
                        AssistedLocationSection(
                            state = state,
                            onIntent = onIntent,
                        )
                    } else {
                        ManualFallbackNotice()
                    }
                }

                ContactSection(state = state, onIntent = onIntent)
                AddressFieldsSection(state = state, onIntent = onIntent)

                AnimatedVisibility(
                    visible = state.fieldError != null,
                    enter = fadeIn(tween(140)) + slideInVertically { it / 3 },
                    exit = fadeOut(tween(100)) + slideOutVertically { it / 3 },
                ) {
                    Text(
                        text = state.fieldError.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag(AddressEditTestTags.FieldError),
                    )
                }

                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn(tween(140)) + slideInVertically { it / 3 },
                    exit = fadeOut(tween(100)) + slideOutVertically { it / 3 },
                ) {
                    Text(
                        text = state.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}

@Composable
private fun AddressEntryModeSwitcher(
    selectedMode: AddressEntryMode,
    onModeSelected: (AddressEntryMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AddressEditTestTags.ModeSwitcher),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AddressModeChip(
            selected = selectedMode == AddressEntryMode.ASSISTED,
            label = stringResource(R.string.address_edit_mode_assisted),
            onClick = { onModeSelected(AddressEntryMode.ASSISTED) },
            modifier = Modifier.weight(1f),
        )
        AddressModeChip(
            selected = selectedMode == AddressEntryMode.MANUAL,
            label = stringResource(R.string.address_edit_mode_manual),
            onClick = { onModeSelected(AddressEntryMode.MANUAL) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun AddressModeChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                modifier = Modifier.fillMaxWidth(),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        },
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(18.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

@Composable
private fun AssistedLocationSection(
    state: AddressEditState,
    onIntent: (AddressEditIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.address_edit_assisted_title),
        subtitle = stringResource(R.string.address_edit_assisted_subtitle),
        modifier = modifier.testTag(AddressEditTestTags.AssistedSection),
    ) {
        MapPickerPreview(
            state = state,
            onOpenMap = { onIntent(AddressEditIntent.OpenMapScreen) },
        )

        AnimatedVisibility(
            visible = state.isAutofilled,
            enter = fadeIn(tween(160)) + slideInVertically { it / 4 },
            exit = fadeOut(tween(100)) + slideOutVertically { it / 4 },
        ) {
            AutofillStatus(state = state)
        }
    }
}

@Composable
private fun PlaceSuggestionRow(
    suggestion: AddressPlaceSuggestion,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(AddressEditTestTags.suggestion(suggestion.id)),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = suggestion.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MapPickerPreview(
    state: AddressEditState,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Default centre: Cairo or whatever coords are already in state.
    val initialLat = state.selectedLatitude ?: 30.0444
    val initialLng = state.selectedLongitude ?: 31.2357

    val viewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(initialLng, initialLat))
            zoom(13.0)
        }
    }

    // Snap camera when state coords change (e.g. suggestion selected).
    LaunchedEffect(state.selectedLatitude, state.selectedLongitude) {
        val lat = state.selectedLatitude ?: return@LaunchedEffect
        val lng = state.selectedLongitude ?: return@LaunchedEffect
        viewportState.easeTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(lng, lat))
                .zoom(14.0)
                .build(),
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AddressEditTestTags.MapPreview),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
        ) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = viewportState,
                onMapLongClickListener = { false },
                content = {
                    val lat = state.selectedLatitude
                    val lng = state.selectedLongitude
                    if (lat != null && lng != null) {
                        CircleAnnotation(
                            point = Point.fromLngLat(lng, lat),
                        ) {
                            circleRadius = 10.0
                            circleColor = Color(0xFFEF4444)
                            circleStrokeWidth = 2.0
                            circleStrokeColor = Color.White
                        }
                    }
                },
            )

            // Transparent overlay to intercept touches and open the map screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onOpenMap)
            )

            // Action button overlaid on the map preview.
            Button(
                onClick = onOpenMap,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .testTag(AddressEditTestTags.UseMapPinButton),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(Icons.Outlined.Map, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.address_edit_search_hint)) // e.g. "Search for location"
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullScreenMapScreen(
    state: AddressEditState,
    onIntent: (AddressEditIntent) -> Unit,
    onClose: () -> Unit,
) {
    val initialLat = state.selectedLatitude ?: 30.0444
    val initialLng = state.selectedLongitude ?: 31.2357

    val viewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(initialLng, initialLat))
            zoom(13.0)
        }
    }

    LaunchedEffect(state.selectedLatitude, state.selectedLongitude) {
        val lat = state.selectedLatitude ?: return@LaunchedEffect
        val lng = state.selectedLongitude ?: return@LaunchedEffect
        viewportState.easeTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(lng, lat))
                .zoom(14.0)
                .build(),
        )
    }

    androidx.activity.compose.BackHandler(onBack = onClose)

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.address_edit_assisted_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = viewportState,
                onMapClickListener = { point ->
                    onIntent(AddressEditIntent.MapPinMoved(point.latitude(), point.longitude()))
                    true
                },
                content = {
                    val lat = state.selectedLatitude
                    val lng = state.selectedLongitude
                    if (lat != null && lng != null) {
                        CircleAnnotation(point = Point.fromLngLat(lng, lat)) {
                            circleRadius = 10.0
                            circleColor = Color(0xFFEF4444)
                            circleStrokeWidth = 2.0
                            circleStrokeColor = Color.White
                        }
                    }
                }
            )

            // Search Bar overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.placeQuery,
                    onValueChange = { onIntent(AddressEditIntent.PlaceQueryChanged(it)) },
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
                    placeholder = { Text(stringResource(R.string.address_edit_search_hint)) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                AnimatedVisibility(
                    visible = state.placeSuggestions.isNotEmpty(),
                    enter = fadeIn(tween(140)) + slideInVertically { it / 4 },
                    exit = fadeOut(tween(100)) + slideOutVertically { it / 4 },
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        state.placeSuggestions.forEach { suggestion ->
                            PlaceSuggestionRow(
                                suggestion = suggestion,
                                onClick = { onIntent(AddressEditIntent.PlaceSuggestionSelected(suggestion)) },
                            )
                        }
                    }
                }
            }

            // Confirm Pin Button
            Button(
                onClick = {
                    onIntent(AddressEditIntent.UseMapPin)
                    onClose()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                )
            ) {
                Text(
                    text = stringResource(R.string.address_edit_use_map_pin).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    } 
}

@Composable
private fun AutofillStatus(
    state: AddressEditState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(AddressEditTestTags.AutofillStatus),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.address_edit_autofill_applied),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (state.hasManualOverride) {
                    stringResource(R.string.address_edit_manual_override)
                } else {
                    stringResource(R.string.address_edit_autofill_editable)
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ManualFallbackNotice(
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.address_edit_manual_title),
        subtitle = stringResource(R.string.address_edit_manual_subtitle),
        modifier = modifier.testTag(AddressEditTestTags.ManualNotice),
    ) {
        Text(
            text = stringResource(R.string.address_edit_manual_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ContactSection(
    state: AddressEditState,
    onIntent: (AddressEditIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.address_edit_contact_title),
        subtitle = stringResource(R.string.address_edit_contact_subtitle),
        modifier = modifier.testTag(AddressEditTestTags.ContactSection),
        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
    ) {
        AddressField(
            value = state.label,
            label = stringResource(R.string.address_edit_label),
            onValueChange = { onIntent(AddressEditIntent.LabelChanged(it)) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AddressField(
                value = state.firstName,
                label = stringResource(R.string.address_edit_first_name),
                onValueChange = { onIntent(AddressEditIntent.FirstNameChanged(it)) },
                modifier = Modifier.weight(1f),
                required = true,
            )
            AddressField(
                value = state.lastName,
                label = stringResource(R.string.address_edit_last_name),
                onValueChange = { onIntent(AddressEditIntent.LastNameChanged(it)) },
                modifier = Modifier.weight(1f),
                required = true,
            )
        }
        AddressField(
            value = state.phone,
            label = stringResource(R.string.address_edit_phone_required),
            onValueChange = { onIntent(AddressEditIntent.PhoneChanged(it)) },
            required = true,
        )
    }
}

@Composable
private fun AddressFieldsSection(
    state: AddressEditState,
    onIntent: (AddressEditIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard(
        title = stringResource(R.string.address_edit_address_title),
        subtitle = stringResource(R.string.address_edit_address_subtitle),
        modifier = modifier.testTag(AddressEditTestTags.AddressSection),
        leadingIcon = { Icon(Icons.Outlined.EditLocationAlt, contentDescription = null) },
    ) {
        AddressField(
            value = state.addressLine1,
            label = stringResource(R.string.address_edit_line1),
            onValueChange = { onIntent(AddressEditIntent.AddressLine1Changed(it)) },
            required = true,
        )
        AddressField(
            value = state.addressLine2,
            label = stringResource(R.string.address_edit_line2),
            onValueChange = { onIntent(AddressEditIntent.AddressLine2Changed(it)) },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AddressField(
                value = state.city,
                label = stringResource(R.string.address_edit_city),
                onValueChange = { onIntent(AddressEditIntent.CityChanged(it)) },
                modifier = Modifier.weight(1f),
                required = true,
            )
            AddressField(
                value = state.stateOrProvince,
                label = stringResource(R.string.address_edit_state),
                onValueChange = { onIntent(AddressEditIntent.StateChanged(it)) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AddressField(
                value = state.country,
                label = stringResource(R.string.address_edit_country),
                onValueChange = { onIntent(AddressEditIntent.CountryChanged(it)) },
                modifier = Modifier.weight(1f),
                required = true,
            )
            AddressField(
                value = state.postalCode,
                label = stringResource(R.string.address_edit_postal_code),
                onValueChange = { onIntent(AddressEditIntent.PostalCodeChanged(it)) },
                modifier = Modifier.weight(1f),
                required = true,
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            leadingIcon()
                        }
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun AddressField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(if (required) "$label *" else label) },
        singleLine = true,
    )
}

@Composable
private fun AddressSaveBar(
    isSaving: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 10.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .height(56.dp)
                .testTag(AddressEditTestTags.SaveButton),
            enabled = !isSaving,
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(horizontal = 18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = stringResource(R.string.address_edit_save).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}



internal object AddressEditTestTags {
    const val Content = "address_edit_content"
    const val ModeSwitcher = "address_edit_mode_switcher"
    const val AssistedSection = "address_edit_assisted_section"
    const val ManualNotice = "address_edit_manual_notice"
    const val SearchField = "address_edit_search_field"
    const val MapPreview = "address_edit_map_preview"
    const val UseMapPinButton = "address_edit_use_map_pin"
    const val AutofillStatus = "address_edit_autofill_status"
    const val ContactSection = "address_edit_contact_section"
    const val AddressSection = "address_edit_address_section"
    const val FieldError = "address_edit_field_error"
    const val SaveButton = "address_edit_save_button"

    fun suggestion(id: String) = "address_edit_suggestion_$id"
}
