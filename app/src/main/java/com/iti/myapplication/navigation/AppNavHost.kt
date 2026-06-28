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
import com.iti.myapplication.ui.product.viewmodel.ProductDetailViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavScreen.UnknownScreen.route,
) {
    NavHost(
        navController = navController,
        modifier = modifier,
        startDestination = startDestination,
    ) {
        composable(NavScreen.UnknownScreen.route) {
            UnknownScreen()
        }

        composable(
            route = NavScreen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.LongType }
            ),
        ) {
            val viewModel: ProductDetailViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            ProductDetailScreen(
                state = state,
                onIntent = viewModel::processIntent,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}