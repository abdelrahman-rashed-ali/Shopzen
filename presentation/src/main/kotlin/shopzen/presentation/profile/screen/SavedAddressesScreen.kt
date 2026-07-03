package shopzen.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.domain.profile.model.Address
import shopzen.presentation.R
import shopzen.presentation.common.components.AuthRequiredDialog
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.profile.intent.AddressListIntent
import shopzen.presentation.profile.state.AddressListState
import shopzen.presentation.profile.viewmodel.AddressListViewModel

@Composable
fun SavedAddressesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToEdit: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddressListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SavedAddressesContent(
        state = state,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToEdit = onNavigateToEdit,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedAddressesContent(
    state: AddressListState,
    onIntent: (AddressListIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToEdit: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.addresses_title),
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
        if (state.isLoading && state.addresses.isEmpty()) {
            LoadingIndicator(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Button(
                        onClick = {
                            if (state.isGuest) {
                                onIntent(AddressListIntent.AddLocationClicked)
                            } else {
                                onNavigateToEdit(null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = null)
                        Text(stringResource(R.string.addresses_add_new).uppercase())
                    }
                }
                if (state.addresses.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.addresses_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(state.addresses, key = { it.id }) { address ->
                        AddressCard(
                            address = address,
                            onEdit = { onNavigateToEdit(address.id) },
                            onDelete = { onIntent(AddressListIntent.RequestDeleteAddress(address.id)) },
                            onSetDefault = { onIntent(AddressListIntent.SetDefaultAddress(address.id)) }
                        )
                    }
                }
                state.error?.let {
                    item { Text(text = it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    if (state.showAuthRequiredDialog) {
        AuthRequiredDialog(
            onLoginClick = onNavigateToLogin,
            onDismiss = { onIntent(AddressListIntent.DismissDialog) }
        )
    }
    if (state.showDeleteDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.addresses_delete_confirm_title),
            message = stringResource(R.string.addresses_delete_confirm_body),
            confirmText = stringResource(R.string.addresses_delete_confirm_cta),
            cancelText = stringResource(R.string.addresses_delete_cancel_cta),
            onConfirm = { onIntent(AddressListIntent.ConfirmDeleteAddress) },
            onDismiss = { onIntent(AddressListIntent.DismissDialog) }
        )
    }
}

@Composable
private fun AddressCard(
    address: Address,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null)
                    Text(
                        text = address.label.ifBlank { address.city },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (address.isDefault) {
                    Text(
                        text = stringResource(R.string.addresses_default_badge),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            Text(
                text = listOf(
                    address.recipientName,
                    address.addressLine1,
                    address.addressLine2,
                    address.city,
                    address.stateOrProvince,
                    address.postalCode,
                    address.country
                ).filterNot { it.isNullOrBlank() }.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = null)
                    Text(stringResource(R.string.addresses_edit))
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                    Text(stringResource(R.string.addresses_delete))
                }
                if (!address.isDefault) {
                    TextButton(onClick = onSetDefault) {
                        Text(stringResource(R.string.addresses_set_default))
                    }
                }
            }
        }
    }
}
