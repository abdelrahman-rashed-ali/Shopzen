package shopzen.presentation.catalog.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.catalog.components.CategoryChips
import shopzen.presentation.catalog.components.FeaturedBanner
import shopzen.presentation.catalog.components.NewArrivalsSection
import shopzen.presentation.catalog.intent.HomeIntent
import shopzen.presentation.catalog.state.HomeState
import shopzen.presentation.catalog.viewmodel.HomeViewModel
import shopzen.presentation.common.components.ErrorScreen
import shopzen.presentation.common.components.LoadingIndicator

/**
 * Stateful/Stateless Home screen composable.
 * Matches the reference design screenshot exactly.
 * Injects HomeViewModel directly using hiltViewModel() inside its signature.
 */
@Composable
fun HomeScreen(
    onNavigateToBrand: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToProducts: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::processIntent

    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { HomeBottomBar() },
        modifier = modifier.background(Color.White)
    ) { innerPadding ->
        when {
            state.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(innerPadding))
            }

            state.error != null -> {
                ErrorScreen(
                    message = state.error.orEmpty(),
                    onRetry = { onIntent(HomeIntent.LoadHomeData) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            else -> {
                HomeContent(
                    state = state,
                    onNavigateToCategory = onCategoryClick@{ categoryId ->
                        onNavigateToCategory(categoryId)
                    },
                    onNavigateToProduct = onProductClick@{ productId ->
                        onNavigateToProduct(productId)
                    },
                    onNavigateToProducts = onNavigateToProducts,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

/**
 * Top App Bar matching the LUXE branding.
 */
@Composable
private fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hamburger Menu Icon
        IconButton(onClick = { /* Open drawer */ }) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = "Menu",
                tint = Color.Black
            )
        }

        // Center LUXE Branding
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Gold brand icon mark
            Box(
                modifier = Modifier
                    .size(8.dp, 15.dp)
                    .background(Color(0xFFC5A85A)) // Luxe Gold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "LUXE",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 4.sp
                ),
                color = Color.Black
            )
        }

        // Notification Bell Icon
        IconButton(onClick = { /* View notifications */ }) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = Color.Black
            )
        }
    }
}

/**
 * Custom Bottom Navigation Bar matching the design.
 * Features Home active state with dot indicator underneath.
 */
@Composable
private fun HomeBottomBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(Color.White)
            .border(width = 0.5.dp, color = Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home (Active with dot indicator below)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                IconButton(onClick = { /* Already on Home */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Home",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Dot indicator for active Home screen
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(Color.Black, shape = CircleShape)
                )
            }

            // Tab 2: Search
            IconButton(onClick = { /* Navigate to Search */ }) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Tab 3: Wishlist
            IconButton(onClick = { /* Navigate to Wishlist */ }) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Tab 4: Cart (Shopping Bag)
            IconButton(onClick = { /* Navigate to Cart */ }) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = "Cart",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Tab 5: Profile
            IconButton(onClick = { /* Navigate to Profile */ }) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        // 1. Featured Banner
        FeaturedBanner(
            bannerImages = state.bannerImages,
            title = "The Art of Elegance",
            subtitle = "Discover our curated collection of timeless pieces designed for the modern connoisseur."
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Categories (Curations)
        CategoryChips(
            categories = state.categories,
            onCategoryClick = onNavigateToCategory
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Best Sellers (New Arrivals / Product Grid)
        NewArrivalsSection(
            products = state.newArrivals,
            onProductClick = onNavigateToProduct,
            onWishlistClick = { /* Wishlist implementation */ },
            onViewAllClick = onNavigateToProducts
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
