package shopzen.presentation.comparison.state

data class ComparisonState(
    val selectedProducts: List<ComparableProductUiModel> = emptyList(),
    val isCompareEnabled: Boolean = false,
    val error: String? = null
)

data class ComparableProductUiModel(
    val productId: String,
    val title: String,
    val imageUrl: String?,
    val price: Double,
    val currency: String,
    val selectedVariantId: String? = null
)

sealed interface ComparisonIntent {
    data class AddProduct(val product: ComparableProductUiModel) : ComparisonIntent
    data class RemoveProduct(val productId: String) : ComparisonIntent
    data object ClearComparison : ComparisonIntent
    data object CompareWithAi : ComparisonIntent
}

data class AiComparisonResultUiModel(
    val productA: ComparableProductUiModel,
    val productB: ComparableProductUiModel,
    val quickVerdict: String,
    val sideBySideSummary: List<ComparisonRowUiModel>,
    val keyDifferences: List<String>,
    val priceAndValue: String,
    val bestFor: List<BestForUiModel>,
    val prosAndCons: ProductProsConsUiModel,
    val finalRecommendation: String
)

data class ComparisonRowUiModel(
    val label: String,
    val productAValue: String,
    val productBValue: String
)

data class BestForUiModel(
    val productTitle: String,
    val reason: String
)

data class ProductProsConsUiModel(
    val productAPros: List<String>,
    val productACons: List<String>,
    val productBPros: List<String>,
    val productBCons: List<String>
)
