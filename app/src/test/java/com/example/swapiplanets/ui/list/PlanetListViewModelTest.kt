package com.example.swapiplanets.ui.list

import com.example.swapiplanets.testutil.FakeFavouritesRepository
import com.example.swapiplanets.testutil.FakePlanetRepository
import com.example.swapiplanets.testutil.FakePlanetUserPreferencesRepository
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

    @Test
    fun initialState_isLoading() = runTest {
        val repo = FakePlanetRepository()
        repo.planetsResult = Result.success(listOf(planet("1")))
        val vm = PlanetListViewModel(
            repo,
            FakeFavouritesRepository(),
            FakePlanetUserPreferencesRepository()
        )

        assertTrue(vm.state.value is UiState.Loading)
    }

    @Test
    fun loadPlanets_success_setsContent() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.success(listOf(planet("1"), planet("2")))
        }
        val vm = PlanetListViewModel(
            repo,
            FakeFavouritesRepository(),
            FakePlanetUserPreferencesRepository()
        )

        advanceUntilIdle()

        val state = vm.state.value
        assertTrue(state is UiState.Content)
        assertEquals(2, (state as UiState.Content).data.size)
    }

    @Test
    fun loadPlanets_error_setsError() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.failure(RuntimeException("boom"))
        }
        val vm = PlanetListViewModel(
            repo,
            FakeFavouritesRepository(),
            FakePlanetUserPreferencesRepository()
        )

        advanceUntilIdle()

        assertTrue(vm.state.value is UiState.Error)
    }

    @Test
    fun retryAfterError_startsNewRequest_andEventuallySuccess() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.failure(RuntimeException("boom"))
        }
        val vm = PlanetListViewModel(
            repo,
            FakeFavouritesRepository(),
            FakePlanetUserPreferencesRepository()
        )
        advanceUntilIdle()
        assertTrue(vm.state.value is UiState.Error)

        repo.planetsResult = Result.success(listOf(planet("10")))
        vm.loadPlanets()
        advanceUntilIdle()

        assertTrue(vm.state.value is UiState.Content)
        assertEquals(2, repo.planetsRequestCount)
    }

    @Test
    fun queryWithNoMatches_setsEmpty_notContentEmptyList() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.success(listOf(planet("1", "Tatooine")))
        }
        val vm = PlanetListViewModel(
            repo,
            FakeFavouritesRepository(),
            FakePlanetUserPreferencesRepository()
        )
        advanceUntilIdle()

        vm.onQueryChange("zzz")
        advanceTimeBy(350)
        advanceUntilIdle()

        assertTrue(vm.state.value is UiState.Empty)
    }
}
