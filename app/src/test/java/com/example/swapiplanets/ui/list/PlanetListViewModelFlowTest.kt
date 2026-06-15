package com.example.swapiplanets.ui.list

import app.cash.turbine.test
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
class PlanetListViewModelFlowTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun state_emitsFilteredContent_whenSearchQueryChanges() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.success(
                listOf(planet("1", "Tatooine"), planet("2", "Naboo"))
            )
        }
        val vm = PlanetListViewModel(
            repository = repo,
            favouritesRepository = FakeFavouritesRepository(),
            userPreferences = FakePlanetUserPreferencesRepository(),
            notesRepository = FakePlanetNotesRepository(),
            favouritesSyncRepository = FakeFavouritesSyncRepository(),
            userStateRepository = FakePlanetUserStateRepository()
        )

        advanceUntilIdle()

        vm.state.test {
            val loaded = awaitItem()
            assertTrue(loaded.content is UiState.Content)
            assertEquals(2, (loaded.content as UiState.Content).data.size)

            vm.onQueryChange("Naboo")
            advanceTimeBy(350)
            advanceUntilIdle()

            val filtered = awaitItem()
            val planets = (filtered.content as UiState.Content).data
            assertEquals(1, planets.size)
            assertEquals("Naboo", planets.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun state_emitsOnlyFavourites_whenFilterEnabled() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.success(
                listOf(planet("1", "Tatooine"), planet("2", "Naboo"))
            )
        }
        val favourites = FakeFavouritesRepository(initial = setOf("2"))
        val preferences = FakePlanetUserPreferencesRepository(onlyFavourites = false)
        val vm = PlanetListViewModel(
            repository = repo,
            favouritesRepository = favourites,
            userPreferences = preferences,
            notesRepository = FakePlanetNotesRepository(),
            favouritesSyncRepository = FakeFavouritesSyncRepository(),
            userStateRepository = FakePlanetUserStateRepository()
        )

        advanceUntilIdle()

        vm.state.test {
            awaitItem()

            preferences.setListOnlyFavourites(true)
            advanceUntilIdle()

            val filtered = awaitItem()
            val planets = (filtered.content as UiState.Content).data
            assertEquals(1, planets.size)
            assertEquals("Naboo", planets.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun state_emitsPinFeedback_whenPinLimitReached() = runTest {
        val repo = FakePlanetRepository().apply {
            planetsResult = Result.success(listOf(planet("1")))
        }
        val userState = FakePlanetUserStateRepository().apply {
            setPinnedIds("a", "b", "c", "d", "e")
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
        vm.togglePin("new-planet")
        advanceUntilIdle()

        assertEquals("You can pin up to 5 planets.", vm.state.value.pinFeedback)
    }
}
