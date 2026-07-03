package shopzen.presentation.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable

enum class MainTab {
    HOME,
    SEARCH,
    WISHLIST,
    CART,
    PROFILE
}

@Composable
fun MainBottomNavigationBar(
    selectedTab: MainTab,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        NavigationBarItem(
            selected = selectedTab == MainTab.HOME,
            onClick = onHomeClick,
            icon = { Icon(Icons.Outlined.Home, contentDescription = null) }
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.SEARCH,
            onClick = onSearchClick,
            icon = { Icon(Icons.Outlined.Search, contentDescription = null) }
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.WISHLIST,
            onClick = onWishlistClick,
            icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) }
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.CART,
            onClick = onCartClick,
            icon = { Icon(Icons.Outlined.ShoppingBag, contentDescription = null) }
        )
        NavigationBarItem(
            selected = selectedTab == MainTab.PROFILE,
            onClick = onProfileClick,
            icon = { Icon(Icons.Outlined.Person, contentDescription = null) }
        )
    }
}
