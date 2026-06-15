package com.example.swapiplanets.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.swapiplanets.ui.collections.CollectionDetailScreen
import com.example.swapiplanets.ui.collections.CollectionDetailViewModel
import com.example.swapiplanets.ui.collections.CollectionsScreen
import com.example.swapiplanets.ui.collections.CollectionsViewModel
import com.example.swapiplanets.ui.detail.PlanetDetailScreen
import com.example.swapiplanets.ui.detail.PlanetDetailViewModel
import com.example.swapiplanets.ui.favourites.FavouritesScreen
import com.example.swapiplanets.ui.favourites.FavouritesViewModel
import com.example.swapiplanets.ui.list.PlanetListScreen
import com.example.swapiplanets.ui.list.PlanetListViewModel
import com.example.swapiplanets.ui.notes.NotesScreen
import com.example.swapiplanets.ui.notes.NotesViewModel
import com.example.swapiplanets.ui.recent.RecentScreen
import com.example.swapiplanets.ui.recent.RecentViewModel
import com.example.swapiplanets.ui.settings.SettingsScreen
import com.example.swapiplanets.ui.settings.SettingsViewModel

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
            val uiState by vm.state.collectAsStateWithLifecycle()
            PlanetListScreen(
                state = uiState.content,
                query = uiState.searchQuery,
                favouriteIds = uiState.favouriteIds,
                onlyFavourites = uiState.onlyFavourites,
                sortNamesDescending = uiState.sortNamesDescending,
                isOfflineData = uiState.isOfflineData,
                planetIdsWithNotes = uiState.planetIdsWithNotes,
                userStates = uiState.userStates,
                pinnedPlanetIds = uiState.pinnedPlanetIds,
                pinFeedback = uiState.pinFeedback,
                onQueryChange = vm::onQueryChange,
                onOnlyFavouritesChange = vm::setListOnlyFavourites,
                onSortOrderChange = vm::setSortNamesDescending,
                onRetry = vm::loadPlanets,
                onFavouritesClick = {
                    navController.navigate(NavRoutes.FAVOURITES) { launchSingleTop = true }
                },
                onSettingsClick = {
                    navController.navigate(NavRoutes.SETTINGS) { launchSingleTop = true }
                },
                onRecentClick = {
                    navController.navigate(NavRoutes.RECENT) { launchSingleTop = true }
                },
                onNotesClick = {
                    navController.navigate(NavRoutes.NOTES) { launchSingleTop = true }
                },
                onCollectionsClick = {
                    navController.navigate(NavRoutes.COLLECTIONS) { launchSingleTop = true }
                },
                onPlanetClick = { planetId ->
                    navController.navigate("${NavRoutes.PLANET_DETAIL}/$planetId")
                },
                onToggleFavourite = vm::toggleFavourite,
                onTogglePin = vm::togglePin
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

        composable(route = NavRoutes.SETTINGS) {
            val vm: SettingsViewModel = hiltViewModel()
            val cacheTtlHours by vm.cacheTtlHours.collectAsStateWithLifecycle()
            val backgroundRefreshEnabled by vm.backgroundRefreshEnabled.collectAsStateWithLifecycle()
            val profiles by vm.profiles.collectAsStateWithLifecycle()
            val activeProfileId by vm.activeProfileId.collectAsStateWithLifecycle()
            val profileFeedback by vm.profileFeedback.collectAsStateWithLifecycle()
            SettingsScreen(
                cacheTtlHours = cacheTtlHours,
                backgroundRefreshEnabled = backgroundRefreshEnabled,
                profiles = profiles,
                activeProfileId = activeProfileId,
                maxProfiles = vm.maxProfiles,
                profileFeedback = profileFeedback,
                onBack = { navController.popBackStack() },
                onCacheTtlChange = vm::setCacheTtlHours,
                onBackgroundRefreshChange = vm::setBackgroundRefreshEnabled,
                onCreateProfile = vm::createProfile,
                onDeleteProfile = vm::deleteProfile,
                onSwitchProfile = vm::switchProfile
            )
        }

        composable(route = NavRoutes.RECENT) {
            val vm: RecentViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            RecentScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onPlanetClick = { planetId ->
                    navController.navigate("${NavRoutes.PLANET_DETAIL}/$planetId")
                }
            )
        }

        composable(route = NavRoutes.NOTES) {
            val vm: NotesViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            NotesScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onPlanetClick = { planetId ->
                    navController.navigate("${NavRoutes.PLANET_DETAIL}/$planetId")
                },
                onDeleteNote = vm::deleteNote
            )
        }

        composable(route = NavRoutes.COLLECTIONS) {
            val vm: CollectionsViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            CollectionsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onCollectionClick = { id ->
                    navController.navigate("${NavRoutes.COLLECTION_DETAIL}/$id")
                },
                onCreateCollection = vm::createCollection,
                onDeleteCollection = vm::deleteCollection
            )
        }

        composable(route = "${NavRoutes.COLLECTION_DETAIL}/{collectionId}") {
            val vm: CollectionDetailViewModel = hiltViewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            CollectionDetailScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onPlanetClick = { planetId ->
                    navController.navigate("${NavRoutes.PLANET_DETAIL}/$planetId")
                },
                onRemovePlanet = vm::removePlanet
            )
        }

        composable(route = "${NavRoutes.PLANET_DETAIL}/{planetId}") { backStackEntry ->
            val vm: PlanetDetailViewModel = hiltViewModel(backStackEntry)
            val collections by vm.collections.collectAsStateWithLifecycle()
            val planetCollectionIds by vm.planetCollectionIds.collectAsStateWithLifecycle()
            val pinMessage by vm.pinMessage.collectAsStateWithLifecycle()
            PlanetDetailScreen(
                state = vm.state,
                isFavourite = vm.isFavourite,
                noteText = vm.noteText,
                isOfflineContent = vm.isOfflineContent,
                isPinned = vm.isPinned,
                rating = vm.rating,
                pinMessage = pinMessage,
                collections = collections,
                planetCollectionIds = planetCollectionIds,
                onBack = { navController.popBackStack() },
                onRetry = vm::loadPlanet,
                onToggleFavourite = vm::toggleFavourite,
                onTogglePin = vm::togglePin,
                onRatingSelected = vm::setRating,
                onNoteChange = vm::onNoteChange,
                onSaveNote = vm::saveNote,
                onToggleCollection = vm::toggleCollectionMembership
            )
        }
    }
}
