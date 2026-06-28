package iti.presentation.catalog.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import iti.presentation.catalog.components.BrandGrid
import iti.presentation.catalog.components.CategoryChips
import iti.presentation.catalog.components.FeaturedBanner
import iti.presentation.catalog.components.NewArrivalsSection
import iti.presentation.catalog.intent.HomeIntent
import iti.presentation.catalog.state.HomeState
import iti.presentation.common.components.ErrorScreen
import iti.presentation.common.components.LoadingIndicator

/**
 * Stateless Home screen composable.
 * Receives state and dispatches intents — contains zero business logic.
 *
 * @param state Current UI state from the ViewModel.
 * @param onIntent Callback to dispatch user intents.
 * @param onNavigateToBrand Callback for brand navigation.
 * @param onNavigateToCategory Callback for category navigation.
 * @param onNavigateToProduct Callback for product detail navigation.
 * @param onNavigateToProducts Callback for "VIEW ALL" navigation.
 */
@Composable
fun HomeScreen(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onNavigateToBrand: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { innerPadding ->
        when {
            state.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(innerPadding))
            }

            state.error != null -> {
                ErrorScreen(
                    message = state.error,
                    onRetry = { onIntent(HomeIntent.LoadHomeData) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            else -> {
                HomeContent(
                    state = state,
                    onNavigateToBrand = onNavigateToBrand,
                    onNavigateToCategory = onNavigateToCategory,
                    onNavigateToProduct = onNavigateToProduct,
                    onNavigateToProducts = onNavigateToProducts,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onNavigateToBrand: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState)
    ) {
        // 1. Featured Banner
        FeaturedBanner(
            bannerImages = state.bannerImages,
            title = "The Art of Elegance",
            subtitle = "Discover our curated collection of timeless pieces designed for the modern connoisseur."
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 2. Categories (Curations)
        CategoryChips(
            categories = state.categories,
            onCategoryClick = onNavigateToCategory
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 3. Brands
        BrandGrid(
            brands = state.brands,
            onBrandClick = onNavigateToBrand
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 4. New Arrivals
        NewArrivalsSection(
            products = state.newArrivals,
            onProductClick = onNavigateToProduct,
            onWishlistClick = { /* Auth guard handled at NavGraph level */ },
            onViewAllClick = onNavigateToProducts
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}
