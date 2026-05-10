package com.example.swapiplanets.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            val state by vm.state.collectAsStateWithLifecycle()
            val query by vm.searchQuery.collectAsStateWithLifecycle()
            val favouriteIds by vm.favouriteIds.collectAsStateWithLifecycle()
            val onlyFavourites by vm.onlyFavourites.collectAsStateWithLifecycle()
            val sortNamesDescending by vm.sortNamesDescending.collectAsStateWithLifecycle()
            PlanetListScreen(
                state = state,
                query = query,
                favouriteIds = favouriteIds,
                onlyFavourites = onlyFavourites,
                sortNamesDescending = sortNamesDescending,
                onQueryChange = vm::onQueryChange,
                onOnlyFavouritesChange = vm::setListOnlyFavourites,
                onSortOrderChange = vm::setSortNamesDescending,
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
            val state by vm.state.collectAsStateWithLifecycle()
            val favouriteIds by vm.favouriteIds.collectAsStateWithLifecycle()
            val sortNamesDescending by vm.sortNamesDescending.collectAsStateWithLifecycle()
            FavouritesScreen(
                state = state,
                favouriteIds = favouriteIds,
                sortNamesDescending = sortNamesDescending,
                onBack = { navController.popBackStack() },
                onRetry = vm::loadFavourites,
                onSortOrderChange = vm::setSortNamesDescending,
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
