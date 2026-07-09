package shopzen.presentation.comparison.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import shopzen.domain.product.usecase.CompareProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import shopzen.presentation.comparison.state.AiComparisonResultUiModel
import shopzen.presentation.comparison.state.BestForUiModel
import shopzen.presentation.comparison.state.ComparableProductUiModel
import shopzen.presentation.comparison.state.ComparisonIntent
import shopzen.presentation.comparison.state.ComparisonRowUiModel
import shopzen.presentation.comparison.state.ComparisonState
import shopzen.presentation.comparison.state.ProductProsConsUiModel

@HiltViewModel
class ComparisonViewModel @Inject constructor(
    private val compareProductsUseCase: CompareProductsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ComparisonState())
    val state: StateFlow<ComparisonState> = _state.asStateFlow()

    private val _resultState = MutableStateFlow<AiComparisonResultUiModel?>(null)
    val resultState: StateFlow<AiComparisonResultUiModel?> = _resultState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun processIntent(intent: ComparisonIntent) {
        when (intent) {
            is ComparisonIntent.AddProduct -> addProduct(intent.product)
            is ComparisonIntent.RemoveProduct -> removeProduct(intent.productId)
            ComparisonIntent.ClearComparison -> clearComparison()
            ComparisonIntent.CompareWithAi -> compareWithAi()
        }
    }

    private fun addProduct(product: ComparableProductUiModel) {
        _state.update { current ->
            if (current.selectedProducts.any { it.productId == product.productId }) {
                current.copy(error = "Product already selected")
            } else if (current.selectedProducts.size >= 2) {
                current.copy(error = "Can only compare 2 products")
            } else {
                val newProducts = current.selectedProducts + product
                current.copy(
                    selectedProducts = newProducts,
                    isCompareEnabled = newProducts.size == 2,
                    error = null
                )
            }
        }
    }

    private fun removeProduct(productId: String) {
        _state.update { current ->
            val newProducts = current.selectedProducts.filter { it.productId != productId }
            current.copy(
                selectedProducts = newProducts,
                isCompareEnabled = newProducts.size == 2,
                error = null
            )
        }
    }

    private fun clearComparison() {
        _state.value = ComparisonState()
        _resultState.value = null
    }

    private fun compareWithAi() {
        val products = _state.value.selectedProducts
        if (products.size != 2) return

        _isLoading.value = true
        _state.update { it.copy(error = null) }

        viewModelScope.launch {
            val result = compareProductsUseCase(
                productAId = products[0].productId,
                productBId = products[1].productId,
                productAVariantId = products[0].selectedVariantId,
                productBVariantId = products[1].selectedVariantId
            )

            result.onSuccess { data ->
                val pA = data.productA
                val pB = data.productB
                
                val uiResult = AiComparisonResultUiModel(
                    productA = products[0],
                    productB = products[1],
                    quickVerdict = "Comparing ${pA.title} and ${pB.title}.",
                    sideBySideSummary = listOf(
                        ComparisonRowUiModel("Price", "${pA.price} ${pA.currency}", "${pB.price} ${pB.currency}"),
                        ComparisonRowUiModel("Availability", if (pA.available) "In Stock" else "Out of Stock", if (pB.available) "In Stock" else "Out of Stock"),
                        ComparisonRowUiModel("Variants", pA.variants.size.toString(), pB.variants.size.toString())
                    ),
                    keyDifferences = listOf(
                        "Price difference: ${kotlin.math.abs(pA.price - pB.price)}",
                        if (pA.variants.size > pB.variants.size) "${pA.title} has more variants" else "${pB.title} has more variants"
                    ),
                    priceAndValue = "Both products offer value based on their price points.",
                    bestFor = listOf(
                        BestForUiModel(pA.title, "Budget choice if cheaper"),
                        BestForUiModel(pB.title, "Alternative option")
                    ),
                    prosAndCons = ProductProsConsUiModel(
                        productAPros = listOf("Available: ${pA.available}"),
                        productACons = listOf(),
                        productBPros = listOf("Available: ${pB.available}"),
                        productBCons = listOf()
                    ),
                    finalRecommendation = "Choose based on price and variant availability."
                )
                _resultState.value = uiResult
            }.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "Failed to compare") }
            }
            _isLoading.value = false
        }
    }
}
