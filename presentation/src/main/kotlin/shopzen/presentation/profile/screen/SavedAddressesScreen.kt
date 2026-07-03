package shopzen.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.domain.profile.model.Address
import shopzen.presentation.R
import shopzen.presentation.profile.intent.AddressListIntent
import shopzen.presentation.profile.viewmodel.AddressListViewModel
import shopzen.presentation.theme.LocalShopzenColors

@Composable
fun SavedAddressesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (addressId: String?) -> Unit,
    viewModel: AddressListViewModel = hiltViewModel(),
) {
    val colors = LocalShopzenColors.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = colors.backgroundPrimary,
        topBar = {
            SavedLocationsTopBar(
                onNavigateBack = onNavigateBack,
                onAddClick = { onNavigateToEdit(null) },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.addresses_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )

            state.error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textError,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = colors.iconPrimary)
                    }
                }

                state.addresses.isEmpty() -> {
                    EmptyLocationsState(
                        onAddClick = { onNavigateToEdit(null) },
                        modifier = Modifier.weight(1f),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        items(state.addresses, key = { it.id }) { address ->
                            AddressCard(
                                address = address,
                                onEdit = { onNavigateToEdit(address.id) },
                                onDelete = { viewModel.processIntent(AddressListIntent.DeleteRequested(address)) },
                                onSetDefault = { viewModel.processIntent(AddressListIntent.SetDefaultRequested(address)) },
                            )
                        }
                    }
                }
            }
        }
    }

    state.addressPendingDelete?.let {
        AlertDialog(
            onDismissRequest = { viewModel.processIntent(AddressListIntent.DismissDeleteDialog) },
            title = { Text(stringResource(R.string.addresses_delete_confirm_title)) },
            text = { Text(stringResource(R.string.addresses_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.processIntent(AddressListIntent.DeleteConfirmed) }) {
                    Text(text = stringResource(R.string.addresses_delete_confirm_cta), color = colors.textError)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.processIntent(AddressListIntent.DismissDeleteDialog) }) {
                    Text(text = stringResource(R.string.addresses_delete_cancel_cta))
                }
            },
            containerColor = colors.surfaceDialog,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
        )
    }
}

@Composable
private fun SavedLocationsTopBar(
    onNavigateBack: () -> Unit,
    onAddClick: () -> Unit,
) {
    val colors = LocalShopzenColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.common_back), tint = colors.iconPrimary)
        }
        Text(
            text = stringResource(R.string.app_logo),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        IconButton(onClick = onAddClick) {
            Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.addresses_add_new), tint = colors.iconPrimary)
        }
    }
}

@Composable
private fun EmptyLocationsState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalShopzenColors.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = colors.iconSecondary)
        Text(
            text = stringResource(R.string.addresses_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = stringResource(R.string.addresses_add_new).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textPrimary,
            modifier = Modifier
                .padding(top = 20.dp)
                .clickable { onAddClick() },
        )
    }
}

@Composable
private fun AddressCard(
    address: Address,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
) {
    val colors = LocalShopzenColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(8.dp), clip = false)
            .background(colors.surfaceCard, RoundedCornerShape(8.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = colors.iconPrimary)
                Column {
                    Text(
                        text = address.label.ifBlank { address.recipientName },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                    )
                    if (address.isDefault) {
                        Text(
                            text = stringResource(R.string.addresses_default_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSuccess,
                        )
                    }
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.addresses_edit), tint = colors.iconSecondary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.addresses_delete), tint = colors.iconDestructive)
                }
            }
        }

        Text(
            text = buildAddressLine(address),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )

        if (!address.isDefault) {
            Text(
                text = stringResource(R.string.addresses_set_default).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                modifier = Modifier.clickable { onSetDefault() },
            )
        }
    }
}

private fun buildAddressLine(address: Address): String =
    listOf(
        address.addressLine1,
        address.addressLine2.orEmpty(),
        address.city,
        address.stateOrProvince.orEmpty(),
        address.postalCode,
        address.country,
    )
        .filter { it.isNotBlank() }
        .joinToString(separator = ", ")
