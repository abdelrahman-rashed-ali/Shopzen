package com.iti.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.iti.myapplication.ui.product.screen.ProductDetailScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavScreen.ProductDetail.route,
) {
    val defaultProductId = if (startDestination.startsWith("main/products/") && startDestination != NavScreen.ProductDetail.route) {
        startDestination.substringAfterLast("/").toLongOrNull() ?: 9648403579031L
    } else {
        9648403579031L
    }

    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = NavScreen.ProductDetail.route,
    ) {
        composable(
            route = NavScreen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") { 
                    type = NavType.LongType 
                    defaultValue = defaultProductId
                }
            ),
        ) {
            ProductDetailScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}