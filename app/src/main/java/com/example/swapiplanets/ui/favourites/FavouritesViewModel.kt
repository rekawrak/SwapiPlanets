package com.example.swapiplanets.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.data.local.FavouritePlanetEntity

import com.example.swapiplanets.domain.model.FavouriteWithCacheStatus
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.PlanetCacheRepository
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

    private val planetCacheRepository: PlanetCacheRepository,
    private val userPreferences: PlanetUserPreferencesRepository
) : ViewModel() {

    private val refreshRequested = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 0)

    init {
        refreshRequested.tryEmit(Unit)
    }

    val state: StateFlow<UiState<List<FavouriteWithCacheStatus>>> = combine(
        favouritesRepository.observeAll(),
        userPreferences.observeCacheTtlHours(),
        userPreferences.observeSortNamesDescending(),
        refreshRequested
    ) { ids, ttlHours, sortDescending, _ ->
        Triple(ids, ttlHours, sortDescending)
    }.flatMapLatest { (ids, ttlHours, sortDescending) ->
        flow {
            if (ids.isEmpty()) {
                emit(UiState.Empty)
                return@flow
            }
            emit(UiState.Loading)
            val entries = (favouritesRepository as? com.example.swapiplanets.data.repository.RoomFavouritesRepository)?.getAllEntries() ?: emptyList()
            val items = buildFavouriteItems(entries, ttlHours)
            val sorted = if (sortDescending) {
                items.sortedByDescending { it.planet.name }
            } else {
                items.sortedBy { it.planet.name }
            }
            emit(
                if (sorted.isEmpty()) {
                    UiState.Error("No cached favourite planets available offline.")
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

    private suspend fun buildFavouriteItems(
        entries: List<FavouritePlanetEntity>,
        ttlHours: Int
    ): List<FavouriteWithCacheStatus> {
        val ids = entries.map { it.planetId }
        val cached = planetCacheRepository.getByIds(ids).associateBy { it.id }
        val now = System.currentTimeMillis()
        val ttlMs = ttlHours.coerceAtLeast(1) * 60L * 60L * 1000L

        return entries.mapNotNull { entry ->
            val planet = cached[entry.planetId]
                ?: runCatching { planetRepository.getPlanetDetail(entry.planetId) }.getOrNull()
                ?: return@mapNotNull null
            val lastSynced = entry.lastSyncedAtMs
            val isStale = lastSynced == null || (now - lastSynced) > ttlMs
            FavouriteWithCacheStatus(
                planet = planet,
                isStale = isStale,
                lastSyncedAtMs = lastSynced
            )
        }
    }
}