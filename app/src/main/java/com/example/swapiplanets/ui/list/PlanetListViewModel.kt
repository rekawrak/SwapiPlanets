package com.example.swapiplanets.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanetListViewModel @Inject constructor(
    private val repository: PlanetRepository
) : ViewModel() {

    var state: UiState<List<Planet>> by mutableStateOf(UiState.Loading)
        private set

    fun loadPlanets() {
        state = UiState.Loading
        viewModelScope.launch {
            try {
                val planets = repository.getPlanets(page = 1)
                state = UiState.Content(planets)
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    init {
        loadPlanets()
    }
}