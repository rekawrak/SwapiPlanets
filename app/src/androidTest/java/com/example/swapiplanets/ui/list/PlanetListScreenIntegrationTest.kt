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
            PlanetListScreen(
                state = UiState.Content(planets),
                query = "",
                favouriteIds = emptySet(),
                onlyFavourites = false,
                sortNamesDescending = false,
                onQueryChange = {},
                onOnlyFavouritesChange = {},
                onSortOrderChange = {},
                onRetry = {},
                onFavouritesClick = {},
                onPlanetClick = {},
                onToggleFavourite = {}
            )
        }

        composeRule.onNodeWithText("Tatooine").assertIsDisplayed()
        composeRule.onNodeWithText("Search by planet name").assertIsDisplayed()
    }

    @Test
    fun errorState_retryButtonInvokesCallback() {
        var retried = false
        composeRule.setContent {
            PlanetListScreen(
                state = UiState.Error("No internet connection. Check your network and retry."),
                query = "",
                favouriteIds = emptySet(),
                onlyFavourites = false,
                sortNamesDescending = false,
                onQueryChange = {},
                onOnlyFavouritesChange = {},
                onSortOrderChange = {},
                onRetry = { retried = true },
                onFavouritesClick = {},
                onPlanetClick = {},
                onToggleFavourite = {}
            )
        }

        composeRule.onNodeWithText("Retry").performClick()
        assertTrue(retried)
    }

    @Test
    fun listItemClick_passesCorrectPlanetId() {
        var clickedId: String? = null
        val planets = listOf(
            Planet(
                id = "5",
                name = "Dagobah",
                climate = "murky",
                terrain = "swamp",
                population = "unknown",
                rotationPeriod = "23",
                orbitalPeriod = "341",
                diameter = "8900"
            )
        )

        composeRule.setContent {
            PlanetListScreen(
                state = UiState.Content(planets),
                query = "",
                favouriteIds = emptySet(),
                onlyFavourites = false,
                sortNamesDescending = false,
                onQueryChange = {},
                onOnlyFavouritesChange = {},
                onSortOrderChange = {},
                onRetry = {},
                onFavouritesClick = {},
                onPlanetClick = { clickedId = it },
                onToggleFavourite = {}
            )
        }

        composeRule.onNodeWithText("Dagobah").performClick()
        assertEquals("5", clickedId)
    }
}
