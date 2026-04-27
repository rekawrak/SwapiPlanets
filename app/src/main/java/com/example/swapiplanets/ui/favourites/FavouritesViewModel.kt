package com.example.swapiplanets.ui.favourites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val planetRepository: PlanetRepository,
    private val favouritesRepository: FavouritesRepository
) : ViewModel() {

    var state: UiState<List<Planet>> by mutableStateOf(UiState.Loading)
        private set

    var favouriteIds: Set<String> by mutableStateOf(emptySet())
        private set

    fun loadFavourites() {
        refreshFavourites()
    }

    private fun observeFavourites() {
        viewModelScope.launch {
            favouritesRepository.observeAll().collect { ids ->
                favouriteIds = ids
                refreshFavourites()
            }
        }
    }

    private fun refreshFavourites() {
        viewModelScope.launch {
            val ids = favouriteIds.toList()
            if (ids.isEmpty()) {
                state = UiState.Empty
                return@launch
            }

            state = UiState.Loading
            val planets = ids.mapNotNull { id ->
                runCatching { planetRepository.getPlanetDetail(id) }.getOrNull()
            }

            state = if (planets.isEmpty()) {
                UiState.Error("Could not load favourite planets. Please try again.")
            } else {
                UiState.Content(planets)
            }
        }
    }

    fun toggleFavourite(id: String) {
        viewModelScope.launch {
            favouritesRepository.toggle(id)
        }
    }

    init {
        observeFavourites()
    }
}
