package com.example.swapiplanets.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ARG_PLANET_ID = "planetId"

@HiltViewModel
class PlanetDetailViewModel @Inject constructor(
    private val repository: PlanetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val planetId: String = checkNotNull(savedStateHandle[ARG_PLANET_ID])

    var state: UiState<Planet> by mutableStateOf(UiState.Loading)
        private set

    fun loadPlanet() {
        state = UiState.Loading
        viewModelScope.launch {
            try {
                val planet = repository.getPlanetDetail(planetId)
                state = UiState.Content(planet)
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    init {
        loadPlanet()
    }
}