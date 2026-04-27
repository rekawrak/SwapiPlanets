package com.example.swapiplanets.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.swapiplanets.ui.detail.PlanetDetailScreen
import com.example.swapiplanets.ui.detail.PlanetDetailViewModel
import com.example.swapiplanets.ui.favourites.FavouritesScreen
import com.example.swapiplanets.ui.favourites.FavouritesViewModel
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
                state = vm.state,
                query = vm.query,
                favouriteIds = vm.favouriteIds,
                onQueryChange = vm::onQueryChange,
                onRetry = vm::loadPlanets,
                onFavouritesClick = {
                    navController.navigate(NavRoutes.FAVOURITES) {
                        launchSingleTop = true
                    }
                },
                onPlanetClick = { planetId ->
                    navController.navigate("${NavRoutes.PLANET_DETAIL}/$planetId")
                },
                onToggleFavourite = vm::toggleFavourite
            )
        }

        composable(route = NavRoutes.FAVOURITES) {
            val vm: FavouritesViewModel = hiltViewModel()
            FavouritesScreen(
                state = vm.state,
                favouriteIds = vm.favouriteIds,
                onBack = { navController.popBackStack() },
                onRetry = vm::loadFavourites,
                onPlanetClick = { planetId ->
                    navController.navigate("${NavRoutes.PLANET_DETAIL}/$planetId")
                },
                onToggleFavourite = vm::toggleFavourite
            )
        }

        composable(
            route = "${NavRoutes.PLANET_DETAIL}/{planetId}"
        ) { backStackEntry ->
            val vm: PlanetDetailViewModel = hiltViewModel(backStackEntry)
            PlanetDetailScreen(
                state = vm.state,
                isFavourite = vm.isFavourite,
                onBack = { navController.popBackStack() },
                onRetry = vm::loadPlanet,
                onToggleFavourite = vm::toggleFavourite
            )
        }
    }
}