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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
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
        viewModelScope.launch {
            try {
                favouriteIds = favouritesRepository.getAll()
                if (favouriteIds.isEmpty()) {
                    state = UiState.Empty
                    return@launch
                }

                state = UiState.Loading
                val planets = favouriteIds.map { id ->
                    async { planetRepository.getPlanetDetail(id) }
                }.awaitAll()

                state = if (planets.isEmpty()) UiState.Empty else UiState.Content(planets)
            } catch (e: Exception) {
                state = UiState.Error(toReadableError(e))
            }
        }
    }

    fun toggleFavourite(id: String) {
        viewModelScope.launch {
            favouriteIds = favouritesRepository.toggle(id)
            val current = (state as? UiState.Content)?.data.orEmpty()
            val updated = current.filterNot { it.id == id }
            state = if (updated.isEmpty()) UiState.Empty else UiState.Content(updated)
        }
    }

    private fun toReadableError(error: Throwable): String {
        return when (error) {
            is IOException -> "No internet connection. Check your network and retry."
            is HttpException -> "Server error (${error.code()}). Please try again."
            else -> "Could not load favourites. Please try again."
        }
    }

    init {
        loadFavourites()
    }
}
