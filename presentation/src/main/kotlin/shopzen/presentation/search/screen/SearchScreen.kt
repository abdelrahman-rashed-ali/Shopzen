package shopzen.presentation.search.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import shopzen.presentation.common.components.ErrorScreen
import shopzen.presentation.common.components.LoadingIndicator
import shopzen.presentation.common.components.MainShellTab
import shopzen.presentation.common.components.ProductCard
import shopzen.presentation.common.components.ShopzenBottomBar
import shopzen.presentation.common.components.ShopzenTopAppBar
import shopzen.presentation.search.components.CollectionCard
import shopzen.presentation.search.components.FilterDialog
import shopzen.presentation.search.components.SuggestionChip
import shopzen.presentation.search.intent.SearchIntent
import shopzen.presentation.search.state.SearchState
import shopzen.presentation.search.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::processIntent

    if (state.showFilterSheet) {
        val brands = state.allProducts.map { it.vendor }.distinct().filter { it.isNotBlank() }
        FilterDialog(
            categories = state.categories,
            brands = brands,
            initialSelectedCategory = state.selectedCategory,
            initialSelectedBrand = state.selectedBrand,
            initialSelectedSortOption = state.selectedSortOption,
            onApply = { category, brand, sortOption ->
                onIntent(SearchIntent.ApplyFilters(category, brand, sortOption))
            },
            onDismiss = {
                onIntent(SearchIntent.ToggleFilterSheet)
            }
        )
    }

    Scaffold(
        topBar = {
            ShopzenTopAppBar(
                onProfileClick = onNavigateToProfile,
                onCartClick = onNavigateToCart,
            )
        },
        bottomBar = {
            ShopzenBottomBar(
                currentTab = MainShellTab.SEARCH,
                onDiscoverClick = onNavigateToHome,
                onSearchClick = onNavigateToSearch,
                onWishlistClick = onNavigateToWishlist,
                onSettingsClick = onNavigateToSettings,
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
                    onRetry = { onIntent(SearchIntent.LoadInitialData) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            else -> {
                SearchContent(
                    state = state,
                    onIntent = onIntent,
                    onNavigateToProduct = onNavigateToProduct,
                    onNavigateToCategory = onNavigateToCategory,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun SearchTopBar() {
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
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Menu",
                    tint = Color.Black
                )
            }

            Text(
                text = "LUMINA",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 4.sp
                ),
                color = Color.Black
            )

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.Black
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color(0xFFEEEEEE))
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchContent(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Search Bar
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onIntent(SearchIntent.UpdateQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = {
                    Text(
                        text = "Search curated collections...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        ),
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFDDDDDD),
                    unfocusedBorderColor = Color(0xFFEEEEEE),
                    focusedContainerColor = Color(0xFFFAFAFA),
                    unfocusedContainerColor = Color(0xFFFAFAFA),
                    cursorColor = Color.Black
                )
            )
        }

        // 2. Suggestions Section
        if (!state.hasSearched) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "SUGGESTIONS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        state.suggestions.forEach { suggestion ->
                            SuggestionChip(
                                label = suggestion,
                                onClick = { onIntent(SearchIntent.SelectSuggestion(suggestion)) }
                            )
                        }
                    }
                }
            }

            // 3. Explore Collections Section
            item {
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore Collections",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = Color.Black
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable { onIntent(SearchIntent.ToggleFilterSheet) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Filters",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = Color.Black
                        )
                    }
                }
            }

            // 4. Collection Cards
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CollectionCardsLayout(
                    categories = state.categories,
                    onCategoryClick = onNavigateToCategory
                )
            }
        } else {
            // Search Results Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.filteredProducts.size} Results",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.Black
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onIntent(SearchIntent.ToggleFilterSheet) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Filters",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = Color.Black
                        )
                    }
                }
            }

            // Active filter chips
            if (state.query.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            label = "\"${state.query}\"",
                            onClick = { onIntent(SearchIntent.ClearFilters) },
                            isSelected = true
                        )
                    }
                }
            }

            // Product Results Grid
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = Color.Black
                            )
                            Text(
                                text = "Try adjusting your search or filters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                // Two-column product grid embedded inside LazyColumn
                val rows = state.filteredProducts.chunked(2)
                items(rows.size) { index ->
                    val rowProducts = rows[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowProducts.forEach { product ->
                            ProductCard(
                                product = product,
                                onProductClick = onNavigateToProduct,
                                onWishlistClick = { },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowProducts.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionCardsLayout(
    categories: List<shopzen.domain.catalog.model.Category>,
    onCategoryClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First card: large hero card
        if (categories.isNotEmpty()) {
            CollectionCard(
                category = categories[0],
                onClick = onCategoryClick,
                subtitle = if (categories[0].title.contains("Timepiece", ignoreCase = true))
                    "Swiss precision" else null,
                aspectRatio = 4f / 3f
            )
        }

        // Second row: two side-by-side smaller cards
        if (categories.size >= 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CollectionCard(
                    category = categories[1],
                    onClick = onCategoryClick,
                    modifier = Modifier.weight(1f),
                    aspectRatio = 3f / 4f
                )
                CollectionCard(
                    category = categories[2],
                    onClick = onCategoryClick,
                    modifier = Modifier.weight(1f),
                    aspectRatio = 3f / 4f
                )
            }
        }

        // Third card: full-width
        if (categories.size >= 4) {
            CollectionCard(
                category = categories[3],
                onClick = onCategoryClick,
                aspectRatio = 16f / 9f
            )
        }

        // Remaining cards (if any)
        categories.drop(4).forEach { category ->
            CollectionCard(
                category = category,
                onClick = onCategoryClick,
                aspectRatio = 16f / 9f
            )
        }
    }
}
