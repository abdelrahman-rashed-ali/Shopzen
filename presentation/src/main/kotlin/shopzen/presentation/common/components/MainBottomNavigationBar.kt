package shopzen.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import shopzen.presentation.theme.LocalShopzenColors

enum class MainTab {
    HOME,
    SEARCH,
    WISHLIST,
    CART,
    PROFILE,
}

@Composable
fun MainBottomNavigationBar(
    selectedTab: MainTab,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {},
    onCartClick: () -> Unit = {},
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalShopzenColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceBottomNav)
            .navigationBarsPadding()
            .height(72.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MainBottomNavigationItem(
            icon = Icons.Outlined.Home,
            selected = selectedTab == MainTab.HOME,
            onClick = onHomeClick,
        )
        MainBottomNavigationItem(
            icon = Icons.Outlined.Search,
            selected = selectedTab == MainTab.SEARCH,
            onClick = onSearchClick,
        )
        MainBottomNavigationItem(
            icon = Icons.Outlined.FavoriteBorder,
            selected = selectedTab == MainTab.WISHLIST,
            onClick = onWishlistClick,
        )
        MainBottomNavigationItem(
            icon = Icons.Outlined.ShoppingBag,
            selected = selectedTab == MainTab.CART,
            onClick = onCartClick,
        )
        MainBottomNavigationItem(
            icon = Icons.Outlined.Person,
            selected = selectedTab == MainTab.PROFILE,
            onClick = onProfileClick,
        )
    }
}

@Composable
private fun MainBottomNavigationItem(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalShopzenColors.current
    val tint = if (selected) colors.navIconSelected else colors.navIconInactive

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(
                    color = if (selected) tint else Color.Transparent,
                    shape = CircleShape,
                ),
        )
    }
}
