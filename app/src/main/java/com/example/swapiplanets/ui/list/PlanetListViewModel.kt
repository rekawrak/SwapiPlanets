package com.example.swapiplanets.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.model.PlanetUserState
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.FavouritesSyncRepository
import com.example.swapiplanets.domain.repository.PlanetNotesRepository
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.domain.repository.PlanetUserPreferencesRepository
import com.example.swapiplanets.domain.repository.PlanetUserStateRepository
import com.example.swapiplanets.domain.repository.PinResult
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    data class Success(val planets: List<Planet>, val isFromCache: Boolean = false) : RemotePlanetsState
    data class Error(val message: String) : RemotePlanetsState
}


data class PlanetListUiState(
    val content: UiState<List<Planet>> = UiState.Loading,
    val searchQuery: String = "",
    val isOfflineData: Boolean = false,
    val planetIdsWithNotes: Set<String> = emptySet(),
    val userStates: Map<String, PlanetUserState> = emptyMap(),
    val pinnedPlanetIds: List<String> = emptyList(),
    val favouriteIds: Set<String> = emptySet(),
    val onlyFavourites: Boolean = false,
    val sortNamesDescending: Boolean = false,
    val pinFeedback: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class PlanetListViewModel @Inject constructor(
    private val repository: PlanetRepository,
    private val favouritesRepository: FavouritesRepository,
    private val userPreferences: PlanetUserPreferencesRepository,
    private val notesRepository: PlanetNotesRepository,
    private val favouritesSyncRepository: FavouritesSyncRepository,
    private val userStateRepository: PlanetUserStateRepository
) : ViewModel() {

    private val listReloadSignals = MutableSharedFlow<Unit>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val searchInput = MutableStateFlow("")

    private val _pinMessage = MutableStateFlow<String?>(null)

    private val remotePlanets: StateFlow<RemotePlanetsState> =
        merge(flowOf(Unit), listReloadSignals).flatMapLatest {
            flow {
                val cached = repository.getCachedPlanets()
                if (cached.isNotEmpty()) {
                    emit(RemotePlanetsState.Success(cached, isFromCache = true))
                } else {
                    emit(RemotePlanetsState.Loading)
                }
                try {
                    val planets = repository.getPlanets(page = 1)
                    emit(RemotePlanetsState.Success(planets, isFromCache = false))
                } catch (e: Exception) {
                    if (cached.isEmpty()) {
                        emit(RemotePlanetsState.Error(toReadableError(e)))
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, RemotePlanetsState.Loading)

    private val debouncedSearch = searchInput
        .debounce { query -> if (query.isBlank()) 0L else SEARCH_DEBOUNCE_MS }
        .distinctUntilChanged()
    private data class ListSlice(
        val remote: RemotePlanetsState,
        val query: String,
        val onlyFavourites: Boolean,
        val favouriteIds: Set<String>,
        val sortDesc: Boolean,
        val pinnedIds: List<String>
    )

    private data class ListSliceCore(
        val remote: RemotePlanetsState,
        val query: String,
        val onlyFavourites: Boolean,
        val favouriteIds: Set<String>,
        val sortDesc: Boolean
    )

    private val listSliceCore: StateFlow<ListSliceCore> = combine(
        remotePlanets,
        debouncedSearch,
        userPreferences.observeListOnlyFavourites(),
        favouritesRepository.observeAll(),
        userPreferences.observeSortNamesDescending()
    ) { remote, query, onlyFavourites, favouriteIds, sortDesc ->
        ListSliceCore(remote, query, onlyFavourites, favouriteIds, sortDesc)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ListSliceCore(
            remote = RemotePlanetsState.Loading,
            query = "",
            onlyFavourites = false,
            favouriteIds = emptySet(),
            sortDesc = false
        )
    )

    private val listSlice: StateFlow<ListSlice> = combine(
        listSliceCore,
        userStateRepository.observePinnedPlanetIds()
    ) { core, pinnedIds ->
        ListSlice(
            remote = core.remote,
            query = core.query,
            onlyFavourites = core.onlyFavourites,
            favouriteIds = core.favouriteIds,
            sortDesc = core.sortDesc,
            pinnedIds = pinnedIds
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ListSlice(
            remote = RemotePlanetsState.Loading,
            query = "",
            onlyFavourites = false,
            favouriteIds = emptySet(),
            sortDesc = false,
            pinnedIds = emptyList()
        )
    )

    private data class ExtrasSlice(
        val planetIdsWithNotes: Set<String>,
        val userStates: Map<String, PlanetUserState>,
        val pinFeedback: String?
    )

    private val extrasSlice: StateFlow<ExtrasSlice> = combine(
        notesRepository.observePlanetIdsWithNotes(),
        userStateRepository.observeAllStates(),
        _pinMessage
    ) { notesIds, states, pinFeedback ->
        ExtrasSlice(notesIds, states, pinFeedback)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ExtrasSlice(emptySet(), emptyMap(), null)
    )


    val state: StateFlow<PlanetListUiState> = combine(
        listSlice,
        extrasSlice
    ) { list, extras ->
        PlanetListUiState(
            content = mapListUi(
                list.remote,
                list.query,
                list.onlyFavourites,
                list.favouriteIds,
                list.sortDesc,
                list.pinnedIds
            ),
            searchQuery = list.query,
            isOfflineData = list.remote is RemotePlanetsState.Success && list.remote.isFromCache,
            planetIdsWithNotes = extras.planetIdsWithNotes,
            userStates = extras.userStates,
            pinnedPlanetIds = list.pinnedIds,
            favouriteIds = list.favouriteIds,
            onlyFavourites = list.onlyFavourites,
            sortNamesDescending = list.sortDesc,
            pinFeedback = extras.pinFeedback
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, PlanetListUiState())

    fun onQueryChange(newQuery: String) {
        searchInput.value = newQuery
    }

    fun loadPlanets() {
        viewModelScope.launch { listReloadSignals.emit(Unit) }
    }

    fun toggleFavourite(id: String) {
        viewModelScope.launch {
            favouritesRepository.toggle(id)
            favouritesSyncRepository.scheduleImmediateSync()
        }
    }

    fun togglePin(id: String) {
        viewModelScope.launch {
            when (userStateRepository.togglePin(id)) {
                PinResult.Pinned -> _pinMessage.value = null
                PinResult.Unpinned -> _pinMessage.value = null
                PinResult.LimitReached -> _pinMessage.value = "You can pin up to 5 planets."
            }
        }
    }

    fun setListOnlyFavourites(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setListOnlyFavourites(enabled) }
    }

    fun setSortNamesDescending(descending: Boolean) {
        viewModelScope.launch { userPreferences.setSortNamesDescending(descending) }
    }

    private fun mapListUi(
        remote: RemotePlanetsState,
        query: String,
        onlyFavourites: Boolean,
        favouriteIds: Set<String>,
        sortDesc: Boolean,
        pinnedIds: List<String>
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
                list = if (sortDesc) list.sortedByDescending { it.name } else list.sortedBy { it.name }
                list = applyPinnedFirst(list, pinnedIds)
                if (list.isEmpty()) UiState.Empty else UiState.Content(list)
            }
        }
    }

    private fun applyPinnedFirst(planets: List<Planet>, pinnedIds: List<String>): List<Planet> {
        if (pinnedIds.isEmpty()) return planets
        val pinnedSet = pinnedIds.toSet()
        val pinned = pinnedIds.mapNotNull { id -> planets.find { it.id == id } }
        val rest = planets.filter { it.id !in pinnedSet }
        return pinned + rest
    }

    private fun toReadableError(error: Throwable): String {
        return when (error) {
            is IOException -> "No internet connection. Check your network and retry."
            is HttpException -> "Server error (${error.code()}). Please try again."
            else -> "Could not load planets. Please try again."
        }
    }
}