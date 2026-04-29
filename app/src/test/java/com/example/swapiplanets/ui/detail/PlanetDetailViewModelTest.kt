package com.example.swapiplanets.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.example.swapiplanets.testutil.FakeFavouritesRepository
import com.example.swapiplanets.testutil.FakePlanetRepository
import com.example.swapiplanets.testutil.MainDispatcherRule
import com.example.swapiplanets.testutil.planet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanetDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun toggleFavourite_usesPlanetIdFromSavedStateHandle() = runTest {
        val repo = FakePlanetRepository().apply {
            detailResults["42"] = Result.success(planet("42"))
        }
        val favouritesRepo = FakeFavouritesRepository()
        val handle = SavedStateHandle(mapOf("planetId" to "42"))
        val vm = PlanetDetailViewModel(repo, favouritesRepo, handle)
        advanceUntilIdle()

        vm.toggleFavourite()
        advanceUntilIdle()

        assertEquals("42", vm.planetId)
        assertEquals(setOf("42"), favouritesRepo.getAll())
    }
}
