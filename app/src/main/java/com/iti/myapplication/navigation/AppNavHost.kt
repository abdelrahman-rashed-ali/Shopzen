package com.iti.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.iti.myapplication.di.CatalogModule
import iti.presentation.catalog.screen.HomeScreen
import iti.presentation.catalog.viewmodel.HomeViewModel

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
            val viewModel: HomeViewModel = viewModel(
                factory = CatalogModule.provideHomeViewModelFactory()
            )
            val state by viewModel.state.collectAsStateWithLifecycle()

            HomeScreen(
                state = state,
                onIntent = viewModel::processIntent,
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