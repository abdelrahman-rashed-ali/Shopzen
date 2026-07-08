package shopzen.presentation.profile.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
    onNavigateToAddresses: () -> Unit,
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
        onNavigateToAddresses = onNavigateToAddresses,
        modifier = modifier,
    )
}

@Composable
internal fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAddresses: () -> Unit,
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
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .testTag(SettingsTestTags.Content),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SettingsHeader()
            PreferenceSection(state = state, onIntent = onIntent)
            AccountActionsSection(
                isGuest = state.isGuest,
                onSync = { onIntent(SettingsIntent.RequestFirebaseSync) },
            )
            SettingsToolsSection(
                onNavigateToAddresses = onNavigateToAddresses,
            )

            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn(tween(140)) + slideInVertically { it / 3 },
                exit = fadeOut(tween(100)) + slideOutVertically { it / 3 },
            ) {
                Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag(SettingsTestTags.Error),
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
private fun SettingsHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.settings_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PreferenceSection(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSectionCard(
        title = stringResource(R.string.profile_preferences_title),
        subtitle = stringResource(R.string.settings_preferences_subtitle),
        modifier = modifier.testTag(SettingsTestTags.Preferences),
    ) {
        PreferenceGroup(
            label = stringResource(R.string.profile_currency_label),
            icon = { Icon(Icons.Outlined.CreditCard, contentDescription = null) },
        ) {
            AppCurrency.entries.forEach { currency ->
                SettingsChoiceChip(
                    selected = state.preferences.currency == currency,
                    label = "${currency.name} ${currency.symbol}",
                    onClick = { onIntent(SettingsIntent.ChangeCurrency(currency)) },
                    modifier = Modifier.testTag(SettingsTestTags.currency(currency.name)),
                )
            }
        }
        PreferenceGroup(
            label = stringResource(R.string.profile_language_label),
            icon = { Icon(Icons.Outlined.Language, contentDescription = null) },
        ) {
            SettingsChoiceChip(
                selected = state.preferences.language == AppLanguage.ENGLISH,
                label = stringResource(R.string.profile_language_english),
                onClick = { onIntent(SettingsIntent.ChangeLanguage(AppLanguage.ENGLISH)) },
            )
            SettingsChoiceChip(
                selected = state.preferences.language == AppLanguage.ARABIC,
                label = stringResource(R.string.profile_language_arabic),
                onClick = { onIntent(SettingsIntent.ChangeLanguage(AppLanguage.ARABIC)) },
            )
        }
        PreferenceGroup(
            label = stringResource(R.string.profile_theme_label),
            icon = { Icon(Icons.Outlined.Palette, contentDescription = null) },
        ) {
            ThemeChip(state, AppTheme.SYSTEM, R.string.profile_theme_system, onIntent)
            ThemeChip(state, AppTheme.LIGHT, R.string.profile_theme_light, onIntent)
            ThemeChip(state, AppTheme.DARK, R.string.profile_theme_dark, onIntent)
        }
    }
}

@Composable
private fun AccountActionsSection(
    isGuest: Boolean,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSectionCard(
        title = stringResource(R.string.settings_account_title),
        subtitle = if (isGuest) {
            stringResource(R.string.settings_account_guest_subtitle)
        } else {
            stringResource(R.string.settings_account_signed_in_subtitle)
        },
        modifier = modifier.testTag(SettingsTestTags.AccountActions),
    ) {
        OutlinedButton(
            onClick = onSync,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag(SettingsTestTags.SyncButton),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.Outlined.CloudSync, contentDescription = null)
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
                text = stringResource(R.string.settings_sync),
                fontWeight = FontWeight.SemiBold,
            )
        }

    }
}

@Composable
private fun SettingsToolsSection(
    onNavigateToAddresses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSectionCard(
        title = stringResource(R.string.settings_tools_title),
        subtitle = stringResource(R.string.settings_tools_subtitle),
        modifier = modifier.testTag(SettingsTestTags.Tools),
    ) {
        SettingsNavigationRow(
            title = stringResource(R.string.settings_saved_addresses),
            subtitle = stringResource(R.string.settings_saved_addresses_subtitle),
            icon = { Icon(Icons.Outlined.HomeWork, contentDescription = null) },
            onClick = onNavigateToAddresses,
            modifier = Modifier.testTag(SettingsTestTags.AddressesButton),
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
            content()
        }
    }
}

@Composable
private fun PreferenceGroup(
    label: String,
    icon: @Composable () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    icon()
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun SettingsChoiceChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 160),
        label = "settings-choice-chip-color",
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier = modifier.drawBehind {
            drawRoundRect(
                color = selectedColor,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
            )
        },
        shape = RoundedCornerShape(18.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}

@Composable
private fun ThemeChip(
    state: SettingsState,
    theme: AppTheme,
    labelRes: Int,
    onIntent: (SettingsIntent) -> Unit,
) {
    SettingsChoiceChip(
        selected = state.preferences.theme == theme,
        label = stringResource(labelRes),
        onClick = { onIntent(SettingsIntent.ChangeTheme(theme)) },
    )
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal object SettingsTestTags {
    const val Content = "settings_content"
    const val Preferences = "settings_preferences"
    const val AccountActions = "settings_account_actions"
    const val Tools = "settings_tools"
    const val SyncButton = "settings_sync_button"
    const val LoginButton = "settings_login_button"
    const val LogoutButton = "settings_logout_button"
    const val AddressesButton = "settings_addresses_button"
    const val Error = "settings_error"

    fun currency(name: String) = "settings_currency_$name"
}
