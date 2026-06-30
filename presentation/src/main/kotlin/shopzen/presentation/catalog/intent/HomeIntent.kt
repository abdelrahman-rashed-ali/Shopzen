package shopzen.presentation.catalog.intent

sealed class HomeIntent {
    object LoadHomeData : HomeIntent()
    data class NavigateToProduct(val productId: String) : HomeIntent()
    data class NavigateToBrand(val brandName: String) : HomeIntent()
    data class NavigateToCategory(val categoryId: String) : HomeIntent()
}
