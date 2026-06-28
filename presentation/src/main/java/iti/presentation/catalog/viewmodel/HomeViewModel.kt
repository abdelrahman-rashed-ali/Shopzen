package iti.presentation.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import iti.domain.catalog.usecase.GetBrandsUseCase
import iti.domain.catalog.usecase.GetCategoriesUseCase
import iti.domain.catalog.usecase.GetProductsUseCase
import iti.presentation.catalog.intent.HomeIntent
import iti.presentation.catalog.state.HomeState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen following the MVI pattern.
 * Calls only UseCases — never repositories directly.
 * All coroutines launched in viewModelScope.
 */
class HomeViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getBrandsUseCase: GetBrandsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        processIntent(HomeIntent.LoadHomeData)
    }

    /**
     * Single entry point for processing user intents.
     */
    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadHomeData -> loadHomeData()
            is HomeIntent.NavigateToBrand,
            is HomeIntent.NavigateToCategory,
            is HomeIntent.NavigateToProduct -> {
                // Navigation intents are handled by the composable via callbacks
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            val productsDeferred = async { getProductsUseCase() }
            val brandsDeferred = async { getBrandsUseCase() }
            val categoriesDeferred = async { getCategoriesUseCase() }

            val productsResult = productsDeferred.await()
            val brandsResult = brandsDeferred.await()
            val categoriesResult = categoriesDeferred.await()

            val products = productsResult.getOrNull().orEmpty()
            val brands = brandsResult.getOrNull().orEmpty()
            val categories = categoriesResult.getOrNull().orEmpty()

            // Derive banner images from the first 5 products' primary images
            val bannerImages = products
                .take(BANNER_IMAGE_COUNT)
                .map { it.imageUrl }
                .filter { it.isNotEmpty() }

            val hasError = productsResult.isFailure
                && brandsResult.isFailure
                && categoriesResult.isFailure

            val errorMessage = if (hasError) {
                productsResult.exceptionOrNull()?.message
                    ?: "Something went wrong. Please try again."
            } else {
                null
            }

            _state.value = _state.value.copy(
                isLoading = false,
                error = errorMessage,
                bannerImages = bannerImages,
                brands = brands,
                categories = categories,
                newArrivals = products
            )
        }
    }

    /**
     * Factory for creating HomeViewModel with use case dependencies.
     * Will be replaced by Hilt @HiltViewModel once DI is configured.
     */
    class Factory(
        private val getProductsUseCase: GetProductsUseCase,
        private val getBrandsUseCase: GetBrandsUseCase,
        private val getCategoriesUseCase: GetCategoriesUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                getProductsUseCase,
                getBrandsUseCase,
                getCategoriesUseCase
            ) as T
        }
    }

    companion object {
        private const val BANNER_IMAGE_COUNT = 5
    }
}
