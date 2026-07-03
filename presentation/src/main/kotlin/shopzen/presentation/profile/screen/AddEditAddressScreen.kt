package shopzen.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.R
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.profile.intent.AddressEditIntent
import shopzen.presentation.profile.state.AddressEditState
import shopzen.presentation.profile.viewmodel.AddressEditViewModel

@Composable
fun AddEditAddressScreen(
    addressId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddressEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(addressId) {
        viewModel.processIntent(AddressEditIntent.LoadAddress(addressId))
    }
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    AddressEditContent(
        state = state,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressEditContent(
    state: AddressEditState,
    onIntent: (AddressEditIntent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
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
                            }
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AddressField(state.label, R.string.address_edit_label) {
                            onIntent(AddressEditIntent.LabelChanged(it))
                        }
                        AddressField(state.recipientName, R.string.address_edit_recipient_name) {
                            onIntent(AddressEditIntent.RecipientNameChanged(it))
                        }
                        AddressField(state.addressLine1, R.string.address_edit_line1) {
                            onIntent(AddressEditIntent.AddressLine1Changed(it))
                        }
                        AddressField(state.addressLine2, R.string.address_edit_line2) {
                            onIntent(AddressEditIntent.AddressLine2Changed(it))
                        }
                        AddressField(state.city, R.string.address_edit_city) {
                            onIntent(AddressEditIntent.CityChanged(it))
                        }
                        AddressField(state.stateOrProvince, R.string.address_edit_state) {
                            onIntent(AddressEditIntent.StateChanged(it))
                        }
                        AddressField(state.postalCode, R.string.address_edit_postal_code) {
                            onIntent(AddressEditIntent.PostalCodeChanged(it))
                        }
                        AddressField(state.country, R.string.address_edit_country) {
                            onIntent(AddressEditIntent.CountryChanged(it))
                        }
                        AddressField(state.phone, R.string.address_edit_phone) {
                            onIntent(AddressEditIntent.PhoneChanged(it))
                        }
                        state.fieldError?.let {
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }
                        Button(
                            onClick = { onIntent(AddressEditIntent.Save) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(R.string.address_edit_save).uppercase())
                        }
                    }
                }
                state.error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun AddressField(
    value: String,
    labelRes: Int,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(labelRes)) },
        singleLine = true
    )
}
