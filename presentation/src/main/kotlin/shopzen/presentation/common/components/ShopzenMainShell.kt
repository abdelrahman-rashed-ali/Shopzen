package shopzen.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import shopzen.presentation.R

enum class MainShellTab {
    DISCOVER,
    SEARCH,
    WISHLIST,
    SETTINGS,
}

@Composable
fun ShopzenTopAppBar(
    onProfileClick: () -> Unit,
    onCartClick: () -> Unit,
    onAssistantClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = stringResource(R.string.nav_profile_cd),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                text = stringResource(R.string.app_logo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onAssistantClick != null) {
                    IconButton(onClick = onAssistantClick) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = stringResource(R.string.nav_ai_assistant_cd),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                IconButton(onClick = onCartClick) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = stringResource(R.string.nav_cart_cd),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
fun ShopzenBottomBar(
    currentTab: MainShellTab,
    onDiscoverClick: () -> Unit,
    onSearchClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navItemColors = androidx.compose.material3.NavigationBarItemDefaults.colors(
        indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
        selectedIconColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        NavigationBarItem(
            selected = currentTab == MainShellTab.DISCOVER,
            onClick = onDiscoverClick,
            colors = navItemColors,
            icon = {
                ShellIcon(
                    selected = currentTab == MainShellTab.DISCOVER,
                    selectedIcon = Icons.Filled.Home,
                    idleIcon = Icons.Outlined.Home,
                    description = stringResource(R.string.nav_discover),
                )
            },
            label = { Text(stringResource(R.string.nav_discover)) }
        )
        NavigationBarItem(
            selected = currentTab == MainShellTab.SEARCH,
            onClick = onSearchClick,
            colors = navItemColors,
            icon = {
                ShellIcon(
                    selected = currentTab == MainShellTab.SEARCH,
                    selectedIcon = Icons.Filled.Search,
                    idleIcon = Icons.Outlined.Search,
                    description = stringResource(R.string.nav_search),
                )
            },
            label = { Text(stringResource(R.string.nav_search)) }
        )
        NavigationBarItem(
            selected = currentTab == MainShellTab.WISHLIST,
            onClick = onWishlistClick,
            colors = navItemColors,
            icon = {
                ShellIcon(
                    selected = currentTab == MainShellTab.WISHLIST,
                    selectedIcon = Icons.Filled.Favorite,
                    idleIcon = Icons.Outlined.FavoriteBorder,
                    description = stringResource(R.string.nav_wishlist),
                )
            },
            label = { Text(stringResource(R.string.nav_wishlist)) }
        )
        NavigationBarItem(
            selected = currentTab == MainShellTab.SETTINGS,
            onClick = onSettingsClick,
            colors = navItemColors,
            icon = {
                ShellIcon(
                    selected = currentTab == MainShellTab.SETTINGS,
                    selectedIcon = Icons.Filled.Settings,
                    idleIcon = Icons.Outlined.Settings,
                    description = stringResource(R.string.nav_settings),
                )
            },
            label = { Text(stringResource(R.string.nav_settings)) }
        )
    }
}

@Composable
private fun ShellIcon(
    selected: Boolean,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    idleIcon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
) {
    Icon(
        imageVector = if (selected) selectedIcon else idleIcon,
        contentDescription = description,
        modifier = Modifier.size(24.dp)
    )
}
