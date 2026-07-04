package shopzen.presentation.brand.intent

sealed class BrandProductsIntent {
    data class LoadProducts(val brandName: String) : BrandProductsIntent()
}
