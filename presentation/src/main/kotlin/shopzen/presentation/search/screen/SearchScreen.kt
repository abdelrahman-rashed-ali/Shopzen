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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Close
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.domain.search.model.SortOption
import shopzen.presentation.R
import shopzen.presentation.search.util.ImageCompressor
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
import shopzen.presentation.comparison.viewmodel.ComparisonViewModel
import shopzen.presentation.comparison.screen.ComparisonTray
import shopzen.presentation.comparison.state.ComparisonIntent
import kotlin.collections.drop

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
    onNavigateToAiComparison: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
    comparisonViewModel: ComparisonViewModel = hiltViewModel(androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val comparisonState by comparisonViewModel.state.collectAsStateWithLifecycle()
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
        modifier = modifier.background(MaterialTheme.colorScheme.background)
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
                Box(modifier = Modifier.fillMaxSize()) {
                    SearchContent(
                        state = state,
                        onIntent = onIntent,
                        onNavigateToProduct = onNavigateToProduct,
                        onNavigateToCategory = onNavigateToCategory,
                        onCompareClick = { product ->
                            comparisonViewModel.processIntent(
                                ComparisonIntent.AddProduct(
                                    shopzen.presentation.comparison.state.ComparableProductUiModel(
                                        productId = product.id,
                                        title = product.title,
                                        imageUrl = product.imageUrl,
                                        price = product.price.toDoubleOrNull() ?: 0.0,
                                        currency = state.currency.symbol
                                    )
                                )
                            )
                        },
                        modifier = Modifier.padding(innerPadding).fillMaxSize()
                    )
                    ComparisonTray(
                        state = comparisonState,
                        onIntent = comparisonViewModel::processIntent,
                        onNavigateToComparison = onNavigateToAiComparison,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = innerPadding.calculateBottomPadding())
                    )
                }
            }
        }
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchContent(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onCompareClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Search Bar
        item {
            val context = LocalContext.current
            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicturePreview()
            ) { bitmap ->
                if (bitmap != null) {
                    onIntent(SearchIntent.SearchByImageBitmap(bitmap))
                }
            }

            val galleryLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                if (uri != null) {
                    onIntent(SearchIntent.SearchByImageUri(uri))
                }
            }

            if (state.selectedImageUri != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = state.selectedImageUri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Searching by image...", modifier = Modifier.weight(1f))
                    IconButton(onClick = { onIntent(SearchIntent.ClearImageSearch) }) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Clear Image Search")
                    }
                }
            }

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (state.selectedImageUri == null) {
                        Row {
                            IconButton(onClick = {
                                // Since we're using TakePicturePreview, we just launch it without a URI.
                                // In a real app we might use TakePicture with a FileProvider URI, but Preview is fine for < 10MB upload if scaled up or we just use it as is.
                                // However, TakePicturePreview returns a small thumbnail. Let's use it for now as it's the simplest standard contract without FileProvider setup.
                                cameraLauncher.launch(null)
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = "Camera",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                galleryLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoLibrary,
                                    contentDescription = "Gallery",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }

        if (state.isImageUploading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.onSurface
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
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onIntent(SearchIntent.ToggleFilterSheet) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Filters",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
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
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try adjusting your search or filters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                id = product.id,
                                title = product.title,
                                category = product.productType,
                                price = product.price,
                                currency = state.currency,
                                imageUrl = product.imageUrl,
                                isFavorite = false,
                                onProductClick = { onNavigateToProduct(product.id) },
                                onFavoriteClick = { /* TODO */ },
                                onCompareClick = { onCompareClick(product) },
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

@Composable
private fun ProductResultsGrid(
    products: List<Product>,
    currency: shopzen.domain.profile.model.AppCurrency = shopzen.domain.profile.model.AppCurrency.USD,
    onNavigateToProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        products.chunked(2).forEach { rowProducts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                rowProducts.forEach { product ->
                    ProductCard(
                        id = product.id,
                        title = product.title,
                        category = product.productType,
                        price = product.price,
                        currency = currency,
                        imageUrl = product.imageUrl,
                        isFavorite = false,
                        onProductClick = { onNavigateToProduct(product.id) },
                        onFavoriteClick = {},
                        modifier = Modifier
                            .weight(1f)
                            .testTag(SearchTestTags.product(product.id)),
                    )
                }
                if (rowProducts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CollectionCardsLayout(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        categories.take(1).forEach { category ->
            CollectionCard(
                category = category,
                onClick = onCategoryClick,
                modifier = Modifier.testTag(SearchTestTags.collection(category.id)),
                subtitle = stringResource(R.string.search_collection_featured),
                aspectRatio = 4f / 3f,
            )
        }
        if (categories.size >= 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                categories.drop(1).take(2).forEach { category ->
                    CollectionCard(
                        category = category,
                        onClick = onCategoryClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(SearchTestTags.collection(category.id)),
                        aspectRatio = 3f / 4f,
                    )
                }
            }
        }
        categories.drop(3).forEach { category ->
            CollectionCard(
                category = category,
                onClick = onCategoryClick,
                modifier = Modifier.testTag(SearchTestTags.collection(category.id)),
                aspectRatio = 16f / 9f,
            )
        }
    }
}

@Composable
private fun SearchSectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
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
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SortOption.displayLabel(): String = when (this) {
    SortOption.DEFAULT -> stringResource(R.string.search_sort_default)
    SortOption.PRICE_LOW_TO_HIGH -> stringResource(R.string.search_sort_price_low_high)
    SortOption.PRICE_HIGH_TO_LOW -> stringResource(R.string.search_sort_price_high_low)
    SortOption.BEST_SELLER -> stringResource(R.string.search_sort_best_seller)
    SortOption.BY_SUB_CATEGORY -> stringResource(R.string.search_sort_by_category)
}

internal object SearchTestTags {
    const val Content = "search_content"
    const val SearchField = "search_field"
    const val FilterButton = "search_filter_button"
    const val Discover = "search_discover"
    const val Results = "search_results"
    const val ActiveFilters = "search_active_filters"
    const val Empty = "search_empty"

    fun collection(id: String) = "search_collection_$id"

    fun product(id: String) = "search_product_$id"
}