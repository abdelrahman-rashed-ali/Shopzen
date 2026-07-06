package shopzen.presentation.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import shopzen.presentation.common.components.ConfirmationDialog
import shopzen.presentation.common.components.MainShellTab
import shopzen.presentation.common.components.ShopzenBottomBar
import shopzen.presentation.common.components.ShopzenTopAppBar
import shopzen.presentation.profile.intent.SettingsIntent
import shopzen.presentation.profile.state.SettingsState
import shopzen.presentation.profile.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateToDiscover: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsContent(
        state = state,
        onIntent = viewModel::processIntent,
        onNavigateToDiscover = onNavigateToDiscover,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToWishlist = onNavigateToWishlist,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToCart = onNavigateToCart,
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier,
    )
}

@Composable
fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            ShopzenTopAppBar(
                onProfileClick = onNavigateToProfile,
                onCartClick = onNavigateToCart,
            )
        },
        bottomBar = {
            ShopzenBottomBar(
                currentTab = MainShellTab.SETTINGS,
                onDiscoverClick = onNavigateToDiscover,
                onSearchClick = onNavigateToSearch,
                onWishlistClick = onNavigateToWishlist,
                onSettingsClick = {},
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.settings_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            PreferenceCard(state = state, onIntent = onIntent)
            AccountCard(
                isGuest = state.isGuest,
                onLogin = onNavigateToLogin,
                onLogout = { onIntent(SettingsIntent.RequestLogout) },
                onSync = { onIntent(SettingsIntent.RequestFirebaseSync) },
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }

    if (state.showAuthRequiredDialog) {
        AuthRequiredDialog(
            onLoginClick = onNavigateToLogin,
            onDismiss = { onIntent(SettingsIntent.DismissDialog) },
        )
    }

    if (state.showLogoutDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.settings_logout_confirm_title),
            message = stringResource(R.string.settings_logout_confirm_body),
            confirmText = stringResource(R.string.settings_logout_confirm_cta),
            dismissText = stringResource(R.string.common_cancel),
            onConfirm = { onIntent(SettingsIntent.ConfirmLogout) },
            onDismiss = { onIntent(SettingsIntent.DismissDialog) },
        )
    }
}

@Composable
private fun PreferenceCard(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.profile_preferences_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            PreferenceGroup(label = stringResource(R.string.profile_currency_label)) {
                AppCurrency.entries.forEach { currency ->
                    FilterChip(
                        selected = state.preferences.currency == currency,
                        onClick = { onIntent(SettingsIntent.ChangeCurrency(currency)) },
                        label = { Text(currency.name) },
                    )
                }
            }
            PreferenceGroup(label = stringResource(R.string.profile_language_label)) {
                FilterChip(
                    selected = state.preferences.language == AppLanguage.ENGLISH,
                    onClick = { onIntent(SettingsIntent.ChangeLanguage(AppLanguage.ENGLISH)) },
                    label = { Text(stringResource(R.string.profile_language_english)) },
                )
                FilterChip(
                    selected = state.preferences.language == AppLanguage.ARABIC,
                    onClick = { onIntent(SettingsIntent.ChangeLanguage(AppLanguage.ARABIC)) },
                    label = { Text(stringResource(R.string.profile_language_arabic)) },
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
private fun AccountCard(
    isGuest: Boolean,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onSync: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_account_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedButton(
                onClick = onSync,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Outlined.CloudSync, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(stringResource(R.string.settings_sync))
            }
            if (isGuest) {
                Button(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Login, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(stringResource(R.string.settings_login))
                }
            } else {
                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(stringResource(R.string.settings_logout))
                }
            }
        }
    }
}

@Composable
private fun PreferenceGroup(
    label: String,
    content: @Composable RowScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun ThemeChip(
    state: SettingsState,
    theme: AppTheme,
    labelRes: Int,
    onIntent: (SettingsIntent) -> Unit,
) {
    FilterChip(
        selected = state.preferences.theme == theme,
        onClick = { onIntent(SettingsIntent.ChangeTheme(theme)) },
        label = { Text(stringResource(labelRes)) },
    )
}
