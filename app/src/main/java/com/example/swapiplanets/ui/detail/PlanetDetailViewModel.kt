package com.example.swapiplanets.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

private const val ARG_PLANET_ID = "planetId"

@HiltViewModel
class PlanetDetailViewModel @Inject constructor(
    private val repository: PlanetRepository,
    private val favouritesRepository: FavouritesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val planetId: String = checkNotNull(savedStateHandle[ARG_PLANET_ID])

    var state: UiState<Planet> by mutableStateOf(UiState.Loading)
        private set

    var isFavourite: Boolean by mutableStateOf(false)
        private set

    fun loadPlanet() {
        state = UiState.Loading
        viewModelScope.launch {
            try {
                val planet = repository.getPlanetDetail(planetId)
                state = UiState.Content(planet)
            } catch (e: Exception) {
                state = UiState.Error(toReadableError(e))
            }
        }
    }

    fun toggleFavourite() {
        viewModelScope.launch {
            favouritesRepository.toggle(planetId)
            isFavourite = favouritesRepository.isFavourite(planetId)
        }
    }

    private fun loadFavouriteState() {
        viewModelScope.launch {
            isFavourite = favouritesRepository.isFavourite(planetId)
        }
    }

    private fun toReadableError(error: Throwable): String {
        return when (error) {
            is IOException -> "No internet connection. Check your network and retry."
            is HttpException -> "Server error (${error.code()}). Please try again."
            else -> "Could not load planet details. Please try again."
        }
    }

    init {
        loadFavouriteState()
        loadPlanet()
    }
}