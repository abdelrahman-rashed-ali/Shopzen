package shopzen.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.R
import shopzen.presentation.profile.intent.AddressEditIntent
import shopzen.presentation.profile.viewmodel.AddressEditViewModel
import shopzen.presentation.theme.LocalShopzenColors

@Composable
fun AddEditAddressScreen(
    addressId: String?,
    onNavigateBack: () -> Unit,
    viewModel: AddressEditViewModel = hiltViewModel(),
) {
    val colors = LocalShopzenColors.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = colors.backgroundPrimary,
        topBar = {
            AddressEditTopBar(
                title = if (state.isEditMode) {
                    stringResource(R.string.address_edit_title_edit)
                } else {
                    stringResource(R.string.address_edit_title_add)
                },
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(8.dp), clip = false)
                    .background(colors.surfaceCard, RoundedCornerShape(8.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AddressField(
                    value = state.label,
                    onValueChange = { viewModel.processIntent(AddressEditIntent.LabelChanged(it)) },
                    label = stringResource(R.string.address_edit_label),
                )
                AddressField(
                    value = state.recipientName,
                    onValueChange = { viewModel.processIntent(AddressEditIntent.RecipientNameChanged(it)) },
                    label = stringResource(R.string.address_edit_recipient_name),
                )
                AddressField(
                    value = state.addressLine1,
                    onValueChange = { viewModel.processIntent(AddressEditIntent.AddressLine1Changed(it)) },
                    label = stringResource(R.string.address_edit_line1),
                )
                AddressField(
                    value = state.addressLine2,
                    onValueChange = { viewModel.processIntent(AddressEditIntent.AddressLine2Changed(it)) },
                    label = stringResource(R.string.address_edit_line2),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AddressField(
                        value = state.city,
                        onValueChange = { viewModel.processIntent(AddressEditIntent.CityChanged(it)) },
                        label = stringResource(R.string.address_edit_city),
                        modifier = Modifier.weight(1f),
                    )
                    AddressField(
                        value = state.stateOrProvince,
                        onValueChange = { viewModel.processIntent(AddressEditIntent.StateOrProvinceChanged(it)) },
                        label = stringResource(R.string.address_edit_state),
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AddressField(
                        value = state.postalCode,
                        onValueChange = { viewModel.processIntent(AddressEditIntent.PostalCodeChanged(it)) },
                        label = stringResource(R.string.address_edit_postal_code),
                        modifier = Modifier.weight(1f),
                    )
                    AddressField(
                        value = state.country,
                        onValueChange = { viewModel.processIntent(AddressEditIntent.CountryChanged(it)) },
                        label = stringResource(R.string.address_edit_country),
                        modifier = Modifier.weight(1f),
                    )
                }
                AddressField(
                    value = state.phone,
                    onValueChange = { viewModel.processIntent(AddressEditIntent.PhoneChanged(it)) },
                    label = stringResource(R.string.address_edit_phone),
                )
            }

            state.error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textError,
                )
            }

            val requiredFieldsFilled = state.recipientName.isNotBlank() &&
                state.addressLine1.isNotBlank() &&
                state.city.isNotBlank() &&
                state.postalCode.isNotBlank() &&
                state.country.isNotBlank()

            Button(
                onClick = { viewModel.processIntent(AddressEditIntent.Submit) },
                enabled = requiredFieldsFilled && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.actionPrimaryBg,
                    contentColor = colors.actionPrimaryFg,
                    disabledContainerColor = colors.actionDisabledBg,
                    disabledContentColor = colors.actionDisabledFg,
                ),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = colors.actionPrimaryFg)
                } else {
                    Text(
                        text = stringResource(R.string.address_edit_save).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AddressEditTopBar(
    title: String,
    onNavigateBack: () -> Unit,
) {
    val colors = LocalShopzenColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = colors.iconPrimary)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun AddressField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalShopzenColors.current
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.surfaceInput,
        unfocusedContainerColor = colors.surfaceInput,
        focusedBorderColor = colors.borderFocus,
        unfocusedBorderColor = colors.borderDefault,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        cursorColor = colors.textPrimary,
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        colors = fieldColors,
        modifier = modifier.fillMaxWidth(),
    )
}
