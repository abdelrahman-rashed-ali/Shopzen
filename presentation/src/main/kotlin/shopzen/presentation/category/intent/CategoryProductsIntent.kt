package shopzen.presentation.category.intent

sealed class CategoryProductsIntent {
    data class LoadProducts(val categoryId: String) : CategoryProductsIntent()
    object Retry : CategoryProductsIntent()
}
