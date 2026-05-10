package com.example.swapiplanets.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.domain.repository.PlanetUserPreferencesRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MS = 300L

private sealed interface RemotePlanetsState {
    data object Loading : RemotePlanetsState
    data class Success(val planets: List<Planet>) : RemotePlanetsState
    data class Error(val message: String) : RemotePlanetsState
}

/**
 * List screen: network reloads are driven by [merge] + [flatMapLatest]; the visible list is
 * [combine]d from remote payload, debounced search, DataStore filter/sort, and favourite ids from Room.
 */
@HiltViewModel
class PlanetListViewModel @Inject constructor(
    private val repository: PlanetRepository,
    private val favouritesRepository: FavouritesRepository,
    private val userPreferences: PlanetUserPreferencesRepository
) : ViewModel() {

    private val listReloadSignals = MutableSharedFlow<Unit>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val searchInput = MutableStateFlow("")

    private val remotePlanets: StateFlow<RemotePlanetsState> =
        merge(
            flowOf(Unit),
            listReloadSignals
        ).flatMapLatest {
            flow {
                emit(RemotePlanetsState.Loading)
                try {
                    val planets = repository.getPlanets(page = 1)
                    emit(RemotePlanetsState.Success(planets))
                } catch (e: Exception) {
                    emit(RemotePlanetsState.Error(toReadableError(e)))
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = RemotePlanetsState.Loading
        )

    private val debouncedSearch = searchInput
        .debounce { query ->
            if (query.isBlank()) 0L else SEARCH_DEBOUNCE_MS
        }
        .distinctUntilChanged()

    val searchQuery: StateFlow<String> = searchInput.asStateFlow()

    val state: StateFlow<UiState<List<Planet>>> = combine(
        remotePlanets,
        debouncedSearch,
        userPreferences.observeListOnlyFavourites(),
        favouritesRepository.observeAll(),
        userPreferences.observeSortNamesDescending()
    ) { remote, query, onlyFavourites, favouriteIds, sortDesc ->
        mapListUi(remote, query, onlyFavourites, favouriteIds, sortDesc)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = UiState.Loading
    )

    val favouriteIds: StateFlow<Set<String>> = favouritesRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val onlyFavourites: StateFlow<Boolean> = userPreferences.observeListOnlyFavourites()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val sortNamesDescending: StateFlow<Boolean> = userPreferences.observeSortNamesDescending()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun onQueryChange(newQuery: String) {
        searchInput.value = newQuery
    }

    fun loadPlanets() {
        viewModelScope.launch {
            listReloadSignals.emit(Unit)
        }
    }

    fun toggleFavourite(id: String) {
        viewModelScope.launch {
            favouritesRepository.toggle(id)
        }
    }

    fun setListOnlyFavourites(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setListOnlyFavourites(enabled)
        }
    }

    fun setSortNamesDescending(descending: Boolean) {
        viewModelScope.launch {
            userPreferences.setSortNamesDescending(descending)
        }
    }

    private fun mapListUi(
        remote: RemotePlanetsState,
        query: String,
        onlyFavourites: Boolean,
        favouriteIds: Set<String>,
        sortDesc: Boolean
    ): UiState<List<Planet>> {
        return when (remote) {
            RemotePlanetsState.Loading -> UiState.Loading
            is RemotePlanetsState.Error -> UiState.Error(remote.message)
            is RemotePlanetsState.Success -> {
                var list = remote.planets
                if (onlyFavourites) {
                    list = list.filter { favouriteIds.contains(it.id) }
                }
                val trimmed = query.trim()
                if (trimmed.isNotEmpty()) {
                    list = list.filter { it.name.contains(trimmed, ignoreCase = true) }
                }
                list = if (sortDesc) {
                    list.sortedByDescending { it.name }
                } else {
                    list.sortedBy { it.name }
                }
                if (list.isEmpty()) UiState.Empty else UiState.Content(list)
            }
        }
    }

    private fun toReadableError(error: Throwable): String {
        return when (error) {
            is IOException -> "No internet connection. Check your network and retry."
            is HttpException -> "Server error (${error.code()}). Please try again."
            else -> "Could not load planets. Please try again."
        }
    }
}
