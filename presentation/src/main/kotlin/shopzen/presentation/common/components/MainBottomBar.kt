package shopzen.presentation.common.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.ui.unit.dp

enum class BottomTab {
    HOME, SEARCH, WISHLIST, CART, PROFILE
}

@Composable
fun MainBottomBar(
    currentTab: BottomTab,
    onTabClick: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home
            BottomTabItem(
                isSelected = currentTab == BottomTab.HOME,
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Outlined.Home,
                contentDescription = "Home",
                onClick = { onTabClick(BottomTab.HOME) }
            )

            // Tab 2: Search
            BottomTabItem(
                isSelected = currentTab == BottomTab.SEARCH,
                icon = Icons.Outlined.Search,
                selectedIcon = Icons.Outlined.Search,
                contentDescription = "Search",
                onClick = { onTabClick(BottomTab.SEARCH) }
            )

            // Tab 3: Wishlist
            BottomTabItem(
                isSelected = currentTab == BottomTab.WISHLIST,
                icon = Icons.Outlined.FavoriteBorder,
                selectedIcon = Icons.Filled.Favorite,
                contentDescription = "Wishlist",
                onClick = { onTabClick(BottomTab.WISHLIST) }
            )

            // Tab 4: Cart
            BottomTabItem(
                isSelected = currentTab == BottomTab.CART,
                icon = Icons.Outlined.ShoppingBag,
                selectedIcon = Icons.Outlined.ShoppingBag,
                contentDescription = "Cart",
                onClick = { onTabClick(BottomTab.CART) }
            )

            // Tab 5: Profile
            BottomTabItem(
                isSelected = currentTab == BottomTab.PROFILE,
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Outlined.Person,
                contentDescription = "Profile",
                onClick = { onTabClick(BottomTab.PROFILE) }
            )
        }
    }
}

@Composable
private fun BottomTabItem(
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight()
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = if (isSelected) selectedIcon else icon,
                contentDescription = contentDescription,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(Color.Black, shape = CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}
