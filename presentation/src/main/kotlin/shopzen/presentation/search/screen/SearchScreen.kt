package shopzen.presentation.search.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.domain.search.model.SortOption
import shopzen.presentation.R
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
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::processIntent

    if (state.showFilterSheet) {
        FilterDialog(
            categories = state.categories,
            brands = state.allProducts.map { it.vendor }.distinct().filter { it.isNotBlank() },
            initialSelectedCategory = state.selectedCategory,
            initialSelectedBrand = state.selectedBrand,
            initialSelectedSortOption = state.selectedSortOption,
            onApply = { category, brand, sortOption ->
                onIntent(SearchIntent.ApplyFilters(category, brand, sortOption))
            },
            onDismiss = { onIntent(SearchIntent.ToggleFilterSheet) },
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
        modifier = modifier.background(MaterialTheme.colorScheme.background),
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingIndicator(modifier = Modifier.padding(innerPadding))
            state.error != null -> ErrorScreen(
                message = state.error.orEmpty(),
                onRetry = { onIntent(SearchIntent.LoadInitialData) },
                modifier = Modifier.padding(innerPadding),
            )

            else -> SearchContent(
                state = state,
                onIntent = onIntent,
                onNavigateToProduct = onNavigateToProduct,
                onNavigateToCategory = onNavigateToCategory,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
internal fun SearchContent(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag(SearchTestTags.Content),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            SearchHeader(
                query = state.query,
                onQueryChange = { onIntent(SearchIntent.UpdateQuery(it)) },
                onFilterClick = { onIntent(SearchIntent.ToggleFilterSheet) },
                selectedImageUri = state.selectedImageUri,
                onCameraClick = { cameraLauncher.launch(null) },
                onGalleryClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClearImageClick = { onIntent(SearchIntent.ClearImageSearch) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }

        if (state.isImageUploading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator()
                }
            }
        }

        item {
            AnimatedContent(
                targetState = state.hasSearched,
                transitionSpec = {
                    (fadeIn(tween(160)) + slideInVertically { it / 10 })
                        .togetherWith(fadeOut(tween(120)) + slideOutVertically { -it / 12 })
                },
                label = "search-mode",
            ) { hasSearched ->
                if (hasSearched) {
                    SearchResultsContent(
                        state = state,
                        onIntent = onIntent,
                        onNavigateToProduct = onNavigateToProduct,
                    )
                } else {
                    SearchDiscoverContent(
                        state = state,
                        onIntent = onIntent,
                        onNavigateToCategory = onNavigateToCategory,
                    )
                }
            }
        }
    }
}


@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    selectedImageUri: android.net.Uri?,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onClearImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.search_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.search_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (selectedImageUri != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = stringResource(R.string.search_selected_image_cd),
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    text = stringResource(R.string.search_image_searching),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClearImageClick) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.search_clear_image_cd),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag(SearchTestTags.SearchField),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                trailingIcon = {
                    if (selectedImageUri == null) {
                        Row {
                            IconButton(onClick = onCameraClick) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = stringResource(R.string.search_camera_cd),
                                )
                            }
                            IconButton(onClick = onGalleryClick) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoLibrary,
                                    contentDescription = stringResource(R.string.search_gallery_cd),
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clickable(onClick = onFilterClick)
                    .testTag(SearchTestTags.FilterButton),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.search_filters),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchDiscoverContent(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    onNavigateToCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(SearchTestTags.Discover),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        SearchSuggestionsSection(
            suggestions = state.suggestions,
            onSuggestionClick = { onIntent(SearchIntent.SelectSuggestion(it)) },
        )
        SearchCollectionsSection(
            categories = state.categories,
            onCategoryClick = onNavigateToCategory,
        )
    }
}

@Composable
private fun SearchSuggestionsSection(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (suggestions.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SearchSectionHeader(
            title = stringResource(R.string.search_suggestions_title),
            subtitle = stringResource(R.string.search_suggestions_subtitle),
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { suggestion ->
                SuggestionChip(
                    label = suggestion,
                    onClick = { onSuggestionClick(suggestion) },
                )
            }
        }
    }
}

@Composable
private fun SearchCollectionsSection(
    categories: List<Category>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (categories.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SearchSectionHeader(
            title = stringResource(R.string.search_collections_title),
            subtitle = stringResource(R.string.search_collections_subtitle),
        )
        CollectionCardsLayout(
            categories = categories,
            onCategoryClick = onCategoryClick,
        )
    }
}

@Composable
private fun SearchResultsContent(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    onNavigateToProduct: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag(SearchTestTags.Results),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SearchResultsHeader(
            resultCount = state.filteredProducts.size,
            onFilterClick = { onIntent(SearchIntent.ToggleFilterSheet) },
        )
        ActiveFiltersRow(
            state = state,
            onClear = { onIntent(SearchIntent.ClearFilters) },
        )
        if (state.filteredProducts.isEmpty()) {
            SearchEmptyResults(onClear = { onIntent(SearchIntent.ClearFilters) })
        } else {
            ProductResultsGrid(
                products = state.filteredProducts,
                onNavigateToProduct = onNavigateToProduct,
            )
        }
    }
}

@Composable
private fun SearchResultsHeader(
    resultCount: Int,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.search_results_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = pluralStringResource(R.plurals.search_results_count, resultCount, resultCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.clickable(onClick = onFilterClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.search_filters),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun ActiveFiltersRow(
    state: SearchState,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sortLabel = if (state.selectedSortOption == SortOption.DEFAULT) {
        null
    } else {
        state.selectedSortOption.displayLabel()
    }
    val activeFilters = listOfNotNull(
        state.query.takeIf { it.isNotBlank() }?.let { "\"$it\"" },
        state.categories.firstOrNull { it.id == state.selectedCategory }?.title,
        state.selectedBrand,
        sortLabel,
    )

    if (activeFilters.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .testTag(SearchTestTags.ActiveFilters),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        activeFilters.forEach { label ->
            SuggestionChip(
                label = label,
                onClick = onClear,
                isSelected = true,
            )
        }
    }
}

@Composable
private fun SearchEmptyResults(
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(SearchTestTags.Empty),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.search_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.search_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.search_clear_filters),
                modifier = Modifier.clickable(onClick = onClear),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ProductResultsGrid(
    products: List<Product>,
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
                        product = product,
                        onProductClick = onNavigateToProduct,
                        onWishlistClick = {},
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
