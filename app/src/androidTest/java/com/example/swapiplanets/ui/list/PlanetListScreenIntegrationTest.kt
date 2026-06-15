package com.example.swapiplanets.ui.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.ui.common.UiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlanetListScreenIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun defaultScreen(
        state: UiState<List<Planet>>,
        onRetry: () -> Unit = {},
        onPlanetClick: (String) -> Unit = {},
        isOfflineData: Boolean = false
    ) {
        PlanetListScreen(
            state = state,
            query = "",
            favouriteIds = emptySet(),
            onlyFavourites = false,
            sortNamesDescending = false,
            isOfflineData = isOfflineData,
            planetIdsWithNotes = emptySet(),
            userStates = emptyMap(),
            pinnedPlanetIds = emptyList(),
            pinFeedback = null,
            onQueryChange = {},
            onOnlyFavouritesChange = {},
            onSortOrderChange = {},
            onRetry = onRetry,
            onFavouritesClick = {},
            onSettingsClick = {},
            onRecentClick = {},
            onNotesClick = {},
            onCollectionsClick = {},
            onPlanetClick = onPlanetClick,
            onToggleFavourite = {},
            onTogglePin = {}
        )
    }

    @Test
    fun contentState_displaysLoadedData() {
        val planets = listOf(
            Planet(
                id = "1",
                name = "Tatooine",
                climate = "arid",
                terrain = "desert",
                population = "200000",
                rotationPeriod = "23",
                orbitalPeriod = "304",
                diameter = "10465"
            )
        )

        composeRule.setContent {
            defaultScreen(UiState.Content(planets))
        }

        composeRule.onNodeWithText("Tatooine").assertIsDisplayed()
        composeRule.onNodeWithText("Search by planet name").assertIsDisplayed()
    }

    @Test
    fun offlineBanner_shownWhenOfflineData() {
        composeRule.setContent {
            defaultScreen(
                state = UiState.Content(listOf(planetStub())),
                isOfflineData = true
            )
        }
        composeRule.onNodeWithText("Offline mode — showing cached planets").assertIsDisplayed()
    }

    @Test
    fun errorState_retryButtonInvokesCallback() {
        var retried = false
        composeRule.setContent {
            defaultScreen(
                state = UiState.Error("No internet connection. Check your network and retry."),
                onRetry = { retried = true }
            )
        }

        composeRule.onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }

    @Test
    fun listItemClick_passesCorrectPlanetId() {
        var clickedId: String? = null
        composeRule.setContent {
            defaultScreen(
                state = UiState.Content(listOf(planetStub(id = "5", name = "Dagobah"))),
                onPlanetClick = { clickedId = it }
            )
        }

        composeRule.onNodeWithText("Dagobah").performClick()
        assertEquals("5", clickedId)
    }

    private fun planetStub(
        id: String = "1",
        name: String = "Tatooine"
    ) = Planet(
        id = id,
        name = name,
        climate = "arid",
        terrain = "desert",
        population = "200000",
        rotationPeriod = "23",
        orbitalPeriod = "304",
        diameter = "10465"
    )
}
