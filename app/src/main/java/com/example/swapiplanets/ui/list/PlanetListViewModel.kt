package com.example.swapiplanets.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PlanetListViewModel @Inject constructor(
    private val repository: PlanetRepository,
    private val favouritesRepository: FavouritesRepository
) : ViewModel() {

    var state: UiState<List<Planet>> by mutableStateOf(UiState.Loading)
        private set

    var query: String by mutableStateOf("")
        private set

    var favouriteIds: Set<String> by mutableStateOf(emptySet())
        private set

    private var allPlanets: List<Planet> = emptyList()

    fun loadPlanets() {
        state = UiState.Loading
        viewModelScope.launch {
            try {
                allPlanets = repository.getPlanets(page = 1)
                applyFilters()
            } catch (e: Exception) {
                state = UiState.Error(toReadableError(e))
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        query = newQuery
        applyFilters()
    }

    fun toggleFavourite(id: String) {
        viewModelScope.launch {
            favouritesRepository.toggle(id)
        }
    }

    private fun observeFavouriteIds() {
        viewModelScope.launch {
            favouritesRepository.observeAll().collect { ids ->
                favouriteIds = ids
            }
        }
    }

    private fun applyFilters() {
        val filtered = allPlanets.filter {
            query.isBlank() || it.name.contains(query.trim(), ignoreCase = true)
        }
        state = if (filtered.isEmpty()) UiState.Empty else UiState.Content(filtered)
    }

    private fun toReadableError(error: Throwable): String {
        return when (error) {
            is IOException -> "No internet connection. Check your network and retry."
            is HttpException -> "Server error (${error.code()}). Please try again."
            else -> "Could not load planets. Please try again."
        }
    }

    init {
        observeFavouriteIds()
        loadPlanets()
    }
}