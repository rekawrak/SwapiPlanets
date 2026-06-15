package com.example.swapiplanets.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.example.swapiplanets.testutil.FakeFavouritesRepository
import com.example.swapiplanets.testutil.FakeFavouritesSyncRepository
import com.example.swapiplanets.testutil.FakePlanetNotesRepository
import com.example.swapiplanets.testutil.FakePlanetCollectionRepository
import com.example.swapiplanets.testutil.FakePlanetUserStateRepository
import com.example.swapiplanets.testutil.MainDispatcherRule
import com.example.swapiplanets.testutil.planet
import com.example.swapiplanets.testutil.FakePlanetRepository
import com.example.swapiplanets.domain.model.RecentVisit
import com.example.swapiplanets.domain.repository.VisitHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanetDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeVisitHistoryRepository : VisitHistoryRepository {
        val visits = mutableListOf<Pair<String, String>>()

        override suspend fun recordVisit(planetId: String, planetName: String) {
            visits.add(planetId to planetName)
        }

        override fun observeRecent(limit: Int) = flowOf(emptyList<RecentVisit>())
    }

    @Test
    fun toggleFavourite_usesPlanetIdFromSavedStateHandle() = runTest {
        val repo = FakePlanetRepository().apply {
            detailResults["42"] = Result.success(planet("42"))
        }
        val favouritesRepo = FakeFavouritesRepository()
        val handle = SavedStateHandle(mapOf("planetId" to "42"))
        val vm = PlanetDetailViewModel(
            repository = repo,
            favouritesRepository = favouritesRepo,
            visitHistoryRepository = FakeVisitHistoryRepository(),
            notesRepository = FakePlanetNotesRepository(),
            favouritesSyncRepository = FakeFavouritesSyncRepository(),
            userStateRepository = FakePlanetUserStateRepository(),
            collectionRepository = FakePlanetCollectionRepository(),
            savedStateHandle = handle
        )
        advanceUntilIdle()

        vm.toggleFavourite()
        advanceUntilIdle()

        assertEquals("42", vm.planetId)
        assertEquals(setOf("42"), favouritesRepo.getAll())
    }

    @Test
    fun loadPlanet_recordsVisitOnSuccess() = runTest {
        val repo = FakePlanetRepository().apply {
            detailResults["3"] = Result.success(planet("3", "Endor"))
        }
        val visits = FakeVisitHistoryRepository()
        val vm = PlanetDetailViewModel(
            repository = repo,
            favouritesRepository = FakeFavouritesRepository(),
            visitHistoryRepository = visits,
            notesRepository = FakePlanetNotesRepository(),
            favouritesSyncRepository = FakeFavouritesSyncRepository(),
            userStateRepository = FakePlanetUserStateRepository(),
            collectionRepository = FakePlanetCollectionRepository(),
            savedStateHandle = SavedStateHandle(mapOf("planetId" to "3"))
        )
        advanceUntilIdle()

        assertEquals(listOf("3" to "Endor"), visits.visits)
    }

    @Test
    fun loadPlanet_marksPlanetAsRead() = runTest {
        val repo = FakePlanetRepository().apply {
            detailResults["7"] = Result.success(planet("7", "Mustafar"))
        }
        val userState = FakePlanetUserStateRepository()
        PlanetDetailViewModel(
            repository = repo,
            favouritesRepository = FakeFavouritesRepository(),
            visitHistoryRepository = FakeVisitHistoryRepository(),
            notesRepository = FakePlanetNotesRepository(),
            favouritesSyncRepository = FakeFavouritesSyncRepository(),
            userStateRepository = userState,
            collectionRepository = FakePlanetCollectionRepository(),
            savedStateHandle = SavedStateHandle(mapOf("planetId" to "7"))
        )
        advanceUntilIdle()

        assertEquals(true, userState.isRead("7"))
    }
}
