package com.iti.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import iti.presentation.catalog.screen.HomeScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: NavScreen = NavScreen.HomeScreen,
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination.route,
    ) {
        composable(NavScreen.UnknownScreen.route) {
            UnknownScreen()
        }

        composable(NavScreen.HomeScreen.route) {
            HomeScreen(
                onNavigateToBrand = { brandName ->
                    // TODO: navController.navigate("main/brands/$brandName")
                },
                onNavigateToCategory = { categoryId ->
                    // TODO: navController.navigate("main/products?category=$categoryId")
                },
                onNavigateToProduct = { productId ->
                    // TODO: navController.navigate("main/products/$productId")
                },
                onNavigateToProducts = {
                    // TODO: navController.navigate("main/products")
                }
            )
        }
    }
}