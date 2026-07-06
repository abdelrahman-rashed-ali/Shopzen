package shopzen.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.domain.profile.model.AppCurrency
import shopzen.domain.profile.model.AppLanguage
import shopzen.domain.profile.model.AppTheme
import shopzen.presentation.R
import shopzen.presentation.common.components.AuthRequiredDialog
import shopzen.presentation.common.components.BottomBarTab
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.components.HomeBottomBar
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.common.components.MainBottomNavigationBar
import shopzen.presentation.common.components.MainTab
import shopzen.presentation.profile.intent.ProfileIntent
import shopzen.presentation.profile.state.ProfileNavigationTarget
import shopzen.presentation.profile.state.ProfileState
import shopzen.presentation.profile.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPersonalDetails: () -> Unit,
    onNavigateToAddresses: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::processIntent

    LaunchedEffect(state.navigationTarget) {
        when (state.navigationTarget) {
            ProfileNavigationTarget.PERSONAL_DETAILS -> {
                onNavigateToPersonalDetails()
                onIntent(ProfileIntent.NavigationHandled)
            }
            ProfileNavigationTarget.SAVED_LOCATIONS -> {
                onNavigateToAddresses()
                onIntent(ProfileIntent.NavigationHandled)
            }
            null -> Unit
        }
    }

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) onSignedOut()
    }

    ProfileContent(
        state = state,
        onIntent = onIntent,
        onNavigateToHome = onNavigateToHome,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToCart = onNavigateToCart,
        onNavigateToWishlist = onNavigateToWishlist,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_logo),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            HomeBottomBar(
                currentTab = BottomBarTab.PROFILE,
                onNavigateToHome = onNavigateToHome,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToWishlist = onNavigateToWishlist,
                onNavigateToCart = onNavigateToCart,
                onNavigateToProfile = { }
            )
        }
    ) { innerPadding ->
        if (state.isLoading && state.profile == null) {
            LoadingIndicator(modifier = Modifier.padding(innerPadding))
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_account_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(
                        R.string.profile_hello_name,
                        state.profile?.fullName?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.profile_guest_name)
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                ProfileIdentity(
                    state = state,
                    onNavigateToLogin = onNavigateToLogin
                )
                PreferencesCard(state = state, onIntent = onIntent)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProfileActionCard(
                        title = stringResource(R.string.profile_personal_details),
                        icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        onClick = { onIntent(ProfileIntent.PersonalDetailsClicked) }
                    )
                    ProfileActionCard(
                        title = stringResource(R.string.profile_saved_addresses),
                        icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        onClick = { onIntent(ProfileIntent.SavedLocationsClicked) }
                    )
                }
                if (!state.isGuest) {
                    TextButton(
                        onClick = { onIntent(ProfileIntent.RequestSignOut) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.profile_sign_out).uppercase(),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (state.showAuthRequiredDialog) {
        AuthRequiredDialog(
            onLoginClick = onNavigateToLogin,
            onDismiss = { onIntent(ProfileIntent.DismissDialog) }
        )
    }

    if (state.showLogoutDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.profile_logout_confirm_title),
            message = stringResource(R.string.profile_logout_confirm_body),
            confirmText = stringResource(R.string.profile_logout_confirm_cta),
            dismissText = stringResource(R.string.profile_logout_cancel_cta),
            onConfirm = {
                onIntent(ProfileIntent.ConfirmSignOut)
            },
            onDismiss = {
                onIntent(ProfileIntent.DismissDialog)
            }
        )
    }
}

@Composable
private fun ProfileIdentity(
    state: ProfileState,
    onNavigateToLogin: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = state.profile?.fullName?.firstOrNull()?.uppercaseChar()?.toString()
                ?: stringResource(R.string.profile_guest_avatar),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Column {
            Text(
                text = state.profile?.email?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.profile_guest_banner),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.isGuest) {
                Text(
                    text = stringResource(R.string.profile_guest_cta),
                    modifier = Modifier.clickable(onClick = onNavigateToLogin),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PreferencesCard(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profile_preferences_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(Icons.Outlined.Language, contentDescription = null)
            }
            PreferenceGroup(label = stringResource(R.string.profile_currency_label)) {
                AppCurrency.entries.forEach { currency ->
                    FilterChip(
                        selected = state.preferences.currency == currency,
                        onClick = { onIntent(ProfileIntent.ChangeCurrency(currency)) },
                        label = { Text(currency.name) }
                    )
                }
            }
            PreferenceGroup(label = stringResource(R.string.profile_language_label)) {
                FilterChip(
                    selected = state.preferences.language == AppLanguage.ENGLISH,
                    onClick = { onIntent(ProfileIntent.ChangeLanguage(AppLanguage.ENGLISH)) },
                    label = { Text(stringResource(R.string.profile_language_english)) }
                )
                FilterChip(
                    selected = state.preferences.language == AppLanguage.ARABIC,
                    onClick = { onIntent(ProfileIntent.ChangeLanguage(AppLanguage.ARABIC)) },
                    label = { Text(stringResource(R.string.profile_language_arabic)) }
                )
            }
            PreferenceGroup(label = stringResource(R.string.profile_theme_label)) {
                ThemeChip(state, AppTheme.SYSTEM, R.string.profile_theme_system, onIntent)
                ThemeChip(state, AppTheme.LIGHT, R.string.profile_theme_light, onIntent)
                ThemeChip(state, AppTheme.DARK, R.string.profile_theme_dark, onIntent)
            }
        }
    }
}

@Composable
private fun PreferenceGroup(
    label: String,
    content: @Composable RowScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun ThemeChip(
    state: ProfileState,
    theme: AppTheme,
    labelRes: Int,
    onIntent: (ProfileIntent) -> Unit
) {
    FilterChip(
        selected = state.preferences.theme == theme,
        onClick = { onIntent(ProfileIntent.ChangeTheme(theme)) },
        label = { Text(stringResource(labelRes)) }
    )
}

@Composable
private fun ProfileActionCard(
    title: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            icon()
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
