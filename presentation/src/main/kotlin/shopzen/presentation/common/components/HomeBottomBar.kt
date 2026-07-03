package shopzen.presentation.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class BottomBarTab {
    HOME,
    SEARCH,
    WISHLIST,
    CART,
    PROFILE
}

@Composable
fun HomeBottomBar(
    currentTab: BottomBarTab,
    onNavigateToHome: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            BottomItem(
                selected = currentTab == BottomBarTab.HOME,
                icon = Icons.Outlined.Home,
                description = "Home",
                onClick = onNavigateToHome
            )

            BottomItem(
                selected = currentTab == BottomBarTab.SEARCH,
                icon = Icons.Outlined.Search,
                description = "Search",
                onClick = onNavigateToSearch
            )

            BottomItem(
                selected = currentTab == BottomBarTab.WISHLIST,
                icon = Icons.Outlined.FavoriteBorder,
                description = "Wishlist",
                onClick = onNavigateToWishlist
            )

            BottomItem(
                selected = currentTab == BottomBarTab.CART,
                icon = Icons.Outlined.ShoppingBag,
                description = "Cart",
                onClick = onNavigateToCart
            )

            BottomItem(
                selected = currentTab == BottomBarTab.PROFILE,
                icon = Icons.Outlined.Person,
                description = "Profile",
                onClick = onNavigateToProfile
            )
        }
    }
}

@Composable
private fun BottomItem(
    selected: Boolean,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                modifier = Modifier.size(24.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        if (selected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}