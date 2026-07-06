package shopzen.presentation.brand.intent

sealed class BrandListIntent {
    data object LoadBrands : BrandListIntent()
}
