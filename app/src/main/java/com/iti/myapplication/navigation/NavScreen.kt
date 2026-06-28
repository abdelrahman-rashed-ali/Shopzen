package com.iti.myapplication.navigation

/**
 * All route definitions for the app. Single source of truth for route strings.
 */
sealed class NavScreen(val route: String) {
    data object ProductDetail : NavScreen("main/products/{productId}") {
        fun createRoute(productId: Long): String = "main/products/$productId"
    }
}