package com.example.swapiplanets.ui.list

import com.example.swapiplanets.testutil.FakeFavouritesRepository
import com.example.swapiplanets.testutil.FakeFavouritesSyncRepository
import com.example.swapiplanets.testutil.FakePlanetNotesRepository
import com.example.swapiplanets.testutil.FakePlanetRepository
import com.example.swapiplanets.testutil.FakePlanetUserPreferencesRepository
import com.example.swapiplanets.testutil.FakePlanetUserStateRepository
import com.example.swapiplanets.testutil.MainDispatcherRule
import com.example.swapiplanets.testutil.planet
import com.example.swapiplanets.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanetListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(repo: FakePlanetRepository): PlanetListViewModel {
        return PlanetListViewModel(
            repository = repo,
            favouritesRepository = FakeFavouritesRepository(),
            userPreferences = FakePlanetUserPreferencesRepository(),
            notesRepository = FakePlanetNotesRepository(),
            favouritesSyncRepository = FakeFavouritesSyncRepository(),
            userStateRepository = FakePlanetUserStateRepository()
        )
    }

    @Test
    fun initialState_isLoading() = runTest {
        val repo = FakePlanetRepository()
        repo.planetsResult = Result.success(listOf(planet("1")))
        val vm = createViewModel(repo)

        assertTrue(vm.state.value.content is UiState.Loading)
    }

    @Test
    fun loadPlanets_success_setsContent() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.success(listOf(planet("1"), planet("2")))
        }
        val vm = createViewModel(repo)

        advanceUntilIdle()

        val content = vm.state.value.content
        assertTrue(content is UiState.Content)
        assertEquals(2, (content as UiState.Content).data.size)
    }

    @Test
    fun loadPlanets_error_setsError() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.failure(RuntimeException("boom"))
        }
        val vm = createViewModel(repo)

        advanceUntilIdle()

        assertTrue(vm.state.value.content is UiState.Error)
    }

    @Test
    fun loadPlanets_withCache_showsContentWhenNetworkFails() = runTest {
        val cached = listOf(planet("1", "Cached Planet"))
        val repo = FakePlanetRepository().apply {
            cachedPlanets = cached
            planetsResult = Result.failure(RuntimeException("offline"))
        }
        val vm = createViewModel(repo)

        advanceUntilIdle()

        assertTrue(vm.state.value.content is UiState.Content)
        assertTrue(vm.state.value.isOfflineData)
    }

    @Test
    fun retryAfterError_startsNewRequest_andEventuallySuccess() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.failure(RuntimeException("boom"))
        }
        val vm = createViewModel(repo)
        advanceUntilIdle()
        assertTrue(vm.state.value.content is UiState.Error)

        repo.planetsResult = Result.success(listOf(planet("10")))
        vm.loadPlanets()
        advanceUntilIdle()

        assertTrue(vm.state.value.content is UiState.Content)
        assertEquals(2, repo.planetsRequestCount)
    }

    @Test
    fun queryWithNoMatches_setsEmpty_notContentEmptyList() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.success(listOf(planet("1", "Tatooine")))
        }
        val vm = createViewModel(repo)
        advanceUntilIdle()

        vm.onQueryChange("zzz")
        advanceTimeBy(350)
        advanceUntilIdle()

        assertTrue(vm.state.value.content is UiState.Empty)
    }

    @Test
    fun pinnedPlanets_appearFirstInList() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.success(
                listOf(planet("1", "Alderaan"), planet("2", "Bespin"), planet("3", "Corellia"))
            )
        }
        val userState = FakePlanetUserStateRepository().apply {
            setPinnedIds("3", "1")
        }
        val vm = PlanetListViewModel(
            repository = repo,
            favouritesRepository = FakeFavouritesRepository(),
            userPreferences = FakePlanetUserPreferencesRepository(),
            notesRepository = FakePlanetNotesRepository(),
            favouritesSyncRepository = FakeFavouritesSyncRepository(),
            userStateRepository = userState
        )
        advanceUntilIdle()

        val content = vm.state.value.content as UiState.Content
        val names = content.data.map { it.name }
        assertEquals(listOf("Corellia", "Alderaan", "Bespin"), names)
    }
}
