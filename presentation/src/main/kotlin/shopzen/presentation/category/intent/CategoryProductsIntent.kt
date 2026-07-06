package shopzen.presentation.category.intent

sealed class CategoryProductsIntent {
    data class LoadProducts(val categoryTitle: String) : CategoryProductsIntent()
    object Retry : CategoryProductsIntent()
}
