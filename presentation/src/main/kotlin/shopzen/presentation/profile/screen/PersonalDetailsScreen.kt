package shopzen.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import shopzen.presentation.R
import shopzen.presentation.profile.intent.PersonalDetailsIntent
import shopzen.presentation.profile.viewmodel.PersonalDetailsViewModel
import shopzen.presentation.theme.LocalShopzenColors

@Composable
fun PersonalDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: PersonalDetailsViewModel = hiltViewModel(),
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
            DetailsTopBar(
                title = stringResource(R.string.personal_details_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ProfilePhotoHeader(
                name = state.fullName,
                email = state.email,
                photoUrl = state.photoUrl,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(8.dp), clip = false)
                    .background(colors.surfaceCard, RoundedCornerShape(8.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ProfileTextField(
                    value = state.fullName,
                    onValueChange = { viewModel.processIntent(PersonalDetailsIntent.NameChanged(it)) },
                    label = stringResource(R.string.personal_details_full_name),
                )

                ProfileTextField(
                    value = state.email,
                    onValueChange = {},
                    label = stringResource(R.string.personal_details_email),
                    enabled = false,
                )

                ProfileTextField(
                    value = state.phone,
                    onValueChange = { viewModel.processIntent(PersonalDetailsIntent.PhoneChanged(it)) },
                    label = stringResource(R.string.personal_details_phone),
                )
            }

            state.error?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textError,
                )
            }

            Button(
                onClick = { viewModel.processIntent(PersonalDetailsIntent.Submit) },
                enabled = state.fullName.isNotBlank() && !state.isLoading,
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
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.actionPrimaryFg,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.personal_details_save).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsTopBar(
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
private fun ProfilePhotoHeader(
    name: String,
    email: String,
    photoUrl: String?,
) {
    val colors = LocalShopzenColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.backgroundSecondary),
            contentAlignment = Alignment.Center,
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = colors.iconSecondary,
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.ifBlank { stringResource(R.string.profile_fallback_name) },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
) {
    val colors = LocalShopzenColors.current
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.surfaceInput,
        unfocusedContainerColor = colors.surfaceInput,
        disabledContainerColor = colors.surfaceInput,
        focusedBorderColor = colors.borderFocus,
        unfocusedBorderColor = colors.borderDefault,
        disabledBorderColor = colors.borderSubtle,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        disabledTextColor = colors.textSecondary,
        cursorColor = colors.textPrimary,
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        colors = fieldColors,
        modifier = Modifier.fillMaxWidth(),
    )
}
