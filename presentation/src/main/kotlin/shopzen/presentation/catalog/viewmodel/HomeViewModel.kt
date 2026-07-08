package shopzen.presentation.catalog.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import shopzen.domain.ads.usecase.GetAdsUseCase
import shopzen.domain.catalog.model.Brand
import shopzen.domain.catalog.model.Category
import shopzen.domain.catalog.model.Product
import shopzen.domain.catalog.usecase.GetBrandsUseCase
import shopzen.domain.catalog.usecase.GetCategoriesUseCase
import shopzen.domain.catalog.usecase.GetProductsUseCase
import shopzen.presentation.catalog.intent.HomeIntent
import shopzen.presentation.catalog.state.HomeState
import javax.inject.Inject

/**
 * ViewModel for the Home screen following the MVI pattern.
 * Calls only UseCases — never repositories directly.
 * All coroutines are launched in viewModelScope.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getBrandsUseCase: GetBrandsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getAdsUseCase: GetAdsUseCase,
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
                // Navigation intents are handled by the composable via callbacks.
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // supervisorScope ensures that if one fetch fails, the others are not cancelled.
                supervisorScope {
                    val productsDeferred = async { getProductsUseCase() }
                    val brandsDeferred = async { getBrandsUseCase() }
                    val categoriesDeferred = async { getCategoriesUseCase() }
                    val adsDeferred = async { getAdsUseCase() }

                    val productsResult = productsDeferred.await()
                    val brandsResult = brandsDeferred.await()
                    val categoriesResult = categoriesDeferred.await()
                    val adsResult = adsDeferred.await()

                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null,
                        bannerImages = homeBannerImages(),
                        brands = brandsResult.getOrNull().orEmpty().ifEmpty { fallbackHomeBrands() },
                        categories = categoriesResult.getOrNull().orEmpty().ifEmpty { fallbackHomeCategories() },
                        newArrivals = productsResult.getOrNull().orEmpty().ifEmpty { fallbackHomeProducts() },
                        ads = adsResult.getOrNull().orEmpty(),
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Something went wrong. Please try again.",
                )
            }
        }
    }
}