package com.example.swapiplanets.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.domain.repository.PlanetUserPreferencesRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val planetRepository: PlanetRepository,
    private val favouritesRepository: FavouritesRepository,
    private val userPreferences: PlanetUserPreferencesRepository
) : ViewModel() {

    private val refreshRequested = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 0)

    init {
        refreshRequested.tryEmit(Unit)
    }

    val state: StateFlow<UiState<List<Planet>>> = combine(
        favouritesRepository.observeAll(),
        userPreferences.observeSortNamesDescending(),
        refreshRequested
    ) { ids, sortDescending, _ ->
        Pair(ids, sortDescending)
    }.flatMapLatest { (ids, sortDescending) ->
        flow {
            if (ids.isEmpty()) {
                emit(UiState.Empty)
                return@flow
            }
            emit(UiState.Loading)
            val planets = ids.mapNotNull { id ->
                runCatching { planetRepository.getPlanetDetail(id) }.getOrNull()
            }
            val sorted = if (sortDescending) {
                planets.sortedByDescending { it.name }
            } else {
                planets.sortedBy { it.name }
            }
            emit(
                if (sorted.isEmpty()) {
                    UiState.Error("Could not load favourite planets. Please try again.")
                } else {
                    UiState.Content(sorted)
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UiState.Loading
    )

    val favouriteIds: StateFlow<Set<String>> = favouritesRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val sortNamesDescending: StateFlow<Boolean> =
        userPreferences.observeSortNamesDescending()
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun loadFavourites() {
        refreshRequested.tryEmit(Unit)
    }

    fun setSortNamesDescending(descending: Boolean) {
        viewModelScope.launch {
            userPreferences.setSortNamesDescending(descending)
        }
    }

    fun toggleFavourite(id: String) {
        viewModelScope.launch {
            favouritesRepository.toggle(id)
        }
    }
}
