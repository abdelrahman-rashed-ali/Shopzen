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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.R
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.profile.intent.PersonalDetailsIntent
import shopzen.presentation.profile.state.PersonalDetailsState
import shopzen.presentation.profile.viewmodel.PersonalDetailsViewModel

@Composable
fun PersonalDetailsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PersonalDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PersonalDetailsContent(
        state = state,
        onIntent = viewModel::processIntent,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PersonalDetailsContent(
    state: PersonalDetailsState,
    onIntent: (PersonalDetailsIntent) -> Unit,
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
                        text = stringResource(R.string.personal_details_title),
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
                        OutlinedTextField(
                            value = state.fullName,
                            onValueChange = { onIntent(PersonalDetailsIntent.FullNameChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.personal_details_full_name)) },
                            isError = state.nameError != null,
                            supportingText = { state.nameError?.let { Text(it) } },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.personal_details_email)) },
                            enabled = false,
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { onIntent(PersonalDetailsIntent.PhoneChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.personal_details_phone)) },
                            singleLine = true
                        )
                        Button(
                            onClick = { onIntent(PersonalDetailsIntent.Save) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(R.string.personal_details_save).uppercase())
                        }
                    }
                }
                state.error?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                if (state.isSaved) {
                    Text(
                        text = "Saved successfully",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
