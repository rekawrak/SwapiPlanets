package com.example.swapiplanets.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.swapiplanets.ui.detail.PlanetDetailScreen
import com.example.swapiplanets.ui.detail.PlanetDetailViewModel
import com.example.swapiplanets.ui.list.PlanetListScreen
import com.example.swapiplanets.ui.list.PlanetListViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.PLANET_LIST
    ) {
        composable(route = NavRoutes.PLANET_LIST) {
            val vm: PlanetListViewModel = hiltViewModel()
            PlanetListScreen(
                viewModel = vm,
                onPlanetClick = { planetId ->
                    navController.navigate("${NavRoutes.PLANET_DETAIL}/$planetId")
                }
            )
        }

        composable(
    route = "${NavRoutes.PLANET_DETAIL}/{planetId}"
) { backStackEntry ->
    val vm: PlanetDetailViewModel = hiltViewModel(backStackEntry)
    PlanetDetailScreen(
        viewModel = vm,
        navController = navController
    )
}
    }
}