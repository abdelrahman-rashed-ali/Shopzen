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
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import coil.compose.AsyncImage
import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.model.AppLanguage
import shopzen.domain.profile.model.AppTheme
import shopzen.domain.profile.model.UserProfile
import shopzen.presentation.R
import shopzen.presentation.common.components.AuthRequiredDialog
import shopzen.presentation.common.components.MainBottomNavigationBar
import shopzen.presentation.common.components.MainTab
import shopzen.presentation.profile.intent.ProfileIntent
import shopzen.presentation.profile.state.ProfileNavigationTarget
import shopzen.presentation.profile.viewmodel.ProfileViewModel
import shopzen.presentation.theme.LocalShopzenColors

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPersonalDetails: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val colors = LocalShopzenColors.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val signedOut by viewModel.signedOut.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.processIntent(ProfileIntent.LoadProfile)
    }

    LaunchedEffect(signedOut) {
        if (signedOut) onSignedOut()
    }

    LaunchedEffect(state.navigationTarget) {
        when (state.navigationTarget) {
            ProfileNavigationTarget.PERSONAL_DETAILS -> {
                viewModel.processIntent(ProfileIntent.NavigationHandled)
                onNavigateToPersonalDetails()
            }

            ProfileNavigationTarget.SAVED_ADDRESSES -> {
                viewModel.processIntent(ProfileIntent.NavigationHandled)
                onNavigateToAddresses()
            }

            null -> Unit
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = colors.backgroundPrimary,
        topBar = { ProfileTopBar() },
        bottomBar = {
            MainBottomNavigationBar(
                selectedTab = MainTab.PROFILE,
                onHomeClick = onNavigateToHome,
                onProfileClick = {},
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            AccountHeader(
                profile = state.profile,
                isGuest = state.isGuest,
            )

            Spacer(Modifier.height(36.dp))

            PreferencesCard(
                selectedCurrency = state.preferences.currency,
                selectedLanguage = state.preferences.language,
                selectedTheme = state.preferences.theme,
                onCurrencySelected = { viewModel.processIntent(ProfileIntent.CurrencySelected(it)) },
                onLanguageSelected = { viewModel.processIntent(ProfileIntent.LanguageSelected(it)) },
                onThemeSelected = { viewModel.processIntent(ProfileIntent.ThemeSelected(it)) },
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ProfileActionTile(
                    title = stringResource(R.string.profile_personal_details),
                    icon = { Icon(Icons.Outlined.ManageAccounts, contentDescription = null) },
                    onClick = { viewModel.processIntent(ProfileIntent.PersonalDetailsClicked) },
                    modifier = Modifier.weight(1f),
                )
                ProfileActionTile(
                    title = stringResource(R.string.profile_saved_addresses),
                    icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                    onClick = { viewModel.processIntent(ProfileIntent.SavedAddressesClicked) },
                    modifier = Modifier.weight(1f),
                )
            }

            state.error?.let {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = it,
                    color = colors.textError,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (state.isLoading) {
                Spacer(Modifier.height(24.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.iconPrimary)
                }
            }

            Spacer(Modifier.height(56.dp))

            Text(
                text = stringResource(R.string.profile_sign_out).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.processIntent(ProfileIntent.SignOutClicked) }
                    .padding(vertical = 16.dp),
            )
        }
    }

    if (state.showAuthRequiredDialog) {
        AuthRequiredDialog(
            onLoginClick = {
                viewModel.processIntent(ProfileIntent.DismissAuthRequiredDialog)
                onNavigateToLogin()
            },
            onDismiss = { viewModel.processIntent(ProfileIntent.DismissAuthRequiredDialog) },
        )
    }

    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.processIntent(ProfileIntent.DismissLogoutDialog) },
            title = { Text(stringResource(R.string.profile_logout_confirm_title)) },
            text = { Text(stringResource(R.string.profile_logout_confirm_body)) },
            confirmButton = {
                TextButton(onClick = { viewModel.processIntent(ProfileIntent.ConfirmSignOut) }) {
                    Text(text = stringResource(R.string.profile_logout_confirm_cta), color = colors.textError)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.processIntent(ProfileIntent.DismissLogoutDialog) }) {
                    Text(text = stringResource(R.string.profile_logout_cancel_cta))
                }
            },
            containerColor = colors.surfaceDialog,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
        )
    }
}

@Composable
private fun ProfileTopBar() {
    val colors = LocalShopzenColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = {}) {
            Icon(Icons.Outlined.Menu, contentDescription = null, tint = colors.iconPrimary)
        }
        Text(
            text = stringResource(R.string.app_logo),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
        IconButton(onClick = {}) {
            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = colors.iconPrimary)
        }
    }
}

@Composable
private fun AccountHeader(
    profile: UserProfile?,
    isGuest: Boolean,
) {
    val colors = LocalShopzenColors.current
    val displayName = profile?.fullName?.takeIf { it.isNotBlank() }
        ?: stringResource(if (isGuest) R.string.profile_guest_name else R.string.profile_fallback_name)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.profile_account_label),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.profile_hello_name, displayName),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
            profile?.email?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        }

        ProfileAvatar(profile = profile)
    }
}

@Composable
private fun ProfileAvatar(profile: UserProfile?) {
    val colors = LocalShopzenColors.current

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(colors.backgroundSecondary),
        contentAlignment = Alignment.Center,
    ) {
        if (!profile?.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = profile?.photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = profile?.fullName?.firstOrNull()?.uppercaseChar()?.toString()
                    ?: stringResource(R.string.profile_guest_avatar),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
private fun PreferencesCard(
    selectedCurrency: AppCurrency,
    selectedLanguage: AppLanguage,
    selectedTheme: AppTheme,
    onCurrencySelected: (AppCurrency) -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppTheme) -> Unit,
) {
    val colors = LocalShopzenColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(8.dp), clip = false)
            .background(colors.surfaceCard, RoundedCornerShape(8.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.profile_preferences_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
            )
            Icon(Icons.Outlined.Language, contentDescription = null, tint = colors.iconSecondary)
        }

        Divider(color = colors.divider)

        PreferenceRow(
            label = stringResource(R.string.profile_currency_label),
            options = AppCurrency.values().toList(),
            selected = selectedCurrency,
            optionLabel = { it.name },
            onSelected = onCurrencySelected,
        )

        PreferenceRow(
            label = stringResource(R.string.profile_language_label),
            options = AppLanguage.values().toList(),
            selected = selectedLanguage,
            optionLabel = {
                when (it) {
                    AppLanguage.ENGLISH -> stringResource(R.string.profile_language_english)
                    AppLanguage.ARABIC -> stringResource(R.string.profile_language_arabic)
                }
            },
            onSelected = onLanguageSelected,
        )

        PreferenceRow(
            label = stringResource(R.string.profile_theme_label),
            options = AppTheme.values().toList(),
            selected = selectedTheme,
            optionLabel = {
                when (it) {
                    AppTheme.SYSTEM -> stringResource(R.string.profile_theme_system)
                    AppTheme.LIGHT -> stringResource(R.string.profile_theme_light)
                    AppTheme.DARK -> stringResource(R.string.profile_theme_dark)
                }
            },
            onSelected = onThemeSelected,
        )
    }
}

@Composable
private fun <T> PreferenceRow(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelected: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = LocalShopzenColors.current.textSecondary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                PreferenceChip(
                    text = optionLabel(option),
                    selected = option == selected,
                    onClick = { onSelected(option) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PreferenceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalShopzenColors.current

    Box(
        modifier = modifier
            .height(40.dp)
            .background(
                color = if (selected) colors.actionPrimaryBg else colors.backgroundSecondary,
                shape = RoundedCornerShape(0.dp),
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) colors.actionPrimaryFg else colors.textPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProfileActionTile(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalShopzenColors.current

    Column(
        modifier = modifier
            .height(156.dp)
            .shadow(6.dp, RoundedCornerShape(8.dp), clip = false)
            .background(colors.surfaceCard, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            icon()
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
        )
    }
}
