package shopzen.presentation.catalog.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
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
import shopzen.presentation.common.components.MainBottomNavigationBar
import shopzen.presentation.common.components.MainTab

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
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::processIntent

    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = {
            MainBottomNavigationBar(
                selectedTab = MainTab.HOME,
                onHomeClick = {},
                onProfileClick = onNavigateToProfile,
            )
        },
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
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
                // Gold and Black emblem / logo mark matching screenshot exactly
                Canvas(modifier = Modifier.size(16.dp, 16.dp)) {
                    // Left black bar
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(0f, 0f),
                        size = Size(width = size.width * 0.25f, height = size.height)
                    )
                    // Middle gold slanted bar
                    val goldPath = Path().apply {
                        moveTo(size.width * 0.35f, 0f)
                        lineTo(size.width * 0.55f, 0f)
                        lineTo(size.width * 0.9f, size.height)
                        lineTo(size.width * 0.7f, size.height)
                        close()
                    }
                    drawPath(
                        path = goldPath,
                        color = Color(0xFFC5A85A)
                    )
                    // Right black bar
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(size.width * 0.75f, 0f),
                        size = Size(width = size.width * 0.25f, height = size.height)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
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
        // Subtle divider line at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color(0xFFEEEEEE))
        )
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
