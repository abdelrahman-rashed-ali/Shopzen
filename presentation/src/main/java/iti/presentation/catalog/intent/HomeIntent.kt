package iti.presentation.catalog.intent

/**
 * Sealed class representing all user and system events on the Home screen.
 */
sealed class HomeIntent {
    /** Triggers initial data load for all home sections. */
    data object LoadHomeData : HomeIntent()

    /** Navigate to the brand products screen. */
    data class NavigateToBrand(val brandName: String) : HomeIntent()

    /** Navigate to the product list filtered by category. */
    data class NavigateToCategory(val categoryId: String) : HomeIntent()

    /** Navigate to the product detail screen. */
    data class NavigateToProduct(val productId: String) : HomeIntent()
}
