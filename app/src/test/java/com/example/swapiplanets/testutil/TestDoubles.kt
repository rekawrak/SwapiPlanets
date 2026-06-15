package com.example.swapiplanets.testutil

import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.FavouritesSyncRepository
import com.example.swapiplanets.domain.repository.PlanetNotesRepository
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.domain.repository.PlanetUserPreferencesRepository
import com.example.swapiplanets.domain.model.PlanetNote
import com.example.swapiplanets.domain.model.PlanetCollection
import com.example.swapiplanets.domain.model.PlanetCollectionItem
import com.example.swapiplanets.domain.model.PlanetUserState
import com.example.swapiplanets.domain.repository.PlanetCollectionRepository
import com.example.swapiplanets.domain.repository.PlanetUserStateRepository
import com.example.swapiplanets.domain.repository.PinResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FakePlanetRepository : PlanetRepository {
    var planetsResult: Result<List<Planet>> = Result.success(emptyList())
    var detailResults: MutableMap<String, Result<Planet>> = mutableMapOf()
    var planetsRequestCount: Int = 0
    var detailRequestIds: MutableList<String> = mutableListOf()
    var cachedPlanets: List<Planet> = emptyList()
    var cachedDetails: MutableMap<String, Planet> = mutableMapOf()

    override suspend fun getPlanets(page: Int): List<Planet> {
        planetsRequestCount += 1
        return planetsResult.getOrThrow()
    }

    override suspend fun getPlanetDetail(id: String): Planet {
        detailRequestIds.add(id)
        return detailResults[id]?.getOrThrow()
            ?: error("No detail stub configured for id=$id")
    }

    override suspend fun getCachedPlanets(): List<Planet> = cachedPlanets

    override suspend fun getCachedPlanetDetail(id: String): Planet? = cachedDetails[id]
}

class FakeFavouritesRepository(initial: Set<String> = emptySet()) : FavouritesRepository {
    private val ids = linkedSetOf<String>().apply { addAll(initial) }
    val flow = MutableStateFlow(ids.toSet())

    override fun observeAll(): Flow<Set<String>> = flow

    override suspend fun getAll(): Set<String> = ids.toSet()

    override suspend fun isFavourite(id: String): Boolean = ids.contains(id)

    override suspend fun toggle(id: String) {
        if (!ids.add(id)) {
            ids.remove(id)
        }
        flow.value = ids.toSet()
    }
}

class FakePlanetUserPreferencesRepository(
    onlyFavourites: Boolean = false,
    sortNamesDescending: Boolean = false,
    cacheTtlHours: Int = 24,
    backgroundRefreshEnabled: Boolean = true
) : PlanetUserPreferencesRepository {
    private val onlyFavouritesFlow = MutableStateFlow(onlyFavourites)
    private val sortDescendingFlow = MutableStateFlow(sortNamesDescending)
    private val cacheTtlFlow = MutableStateFlow(cacheTtlHours)
    private val backgroundRefreshFlow = MutableStateFlow(backgroundRefreshEnabled)

    override fun observeListOnlyFavourites(): Flow<Boolean> = onlyFavouritesFlow

    override suspend fun setListOnlyFavourites(enabled: Boolean) {
        onlyFavouritesFlow.value = enabled
    }

    override fun observeSortNamesDescending(): Flow<Boolean> = sortDescendingFlow

    override suspend fun setSortNamesDescending(descending: Boolean) {
        sortDescendingFlow.value = descending
    }

    override fun observeCacheTtlHours(): Flow<Int> = cacheTtlFlow

    override suspend fun setCacheTtlHours(hours: Int) {
        cacheTtlFlow.value = hours
    }

    override fun observeBackgroundRefreshEnabled(): Flow<Boolean> = backgroundRefreshFlow

    override suspend fun setBackgroundRefreshEnabled(enabled: Boolean) {
        backgroundRefreshFlow.value = enabled
    }
}

fun planet(
    id: String,
    name: String = "Planet $id"
): Planet = Planet(
    id = id,
    name = name,
    climate = "temperate",
    terrain = "forests",
    population = "1000",
    rotationPeriod = "24",
    orbitalPeriod = "365",
    diameter = "10000"
)

class FakePlanetNotesRepository : PlanetNotesRepository {
    private val notes = mutableMapOf<String, String>()
    private val notesFlow = MutableStateFlow<Set<String>>(emptySet())

    override fun observeNote(planetId: String): Flow<String?> {
        return notesFlow.map { notes[planetId] }
    }

    override fun observeAllNotes(): Flow<List<PlanetNote>> {
        return notesFlow.map {
            notes.map { (id, text) ->
                PlanetNote(id, "Planet $id", text, 0L)
            }
        }
    }

    override fun observePlanetIdsWithNotes(): Flow<Set<String>> = notesFlow

    override suspend fun saveNote(planetId: String, planetName: String, noteText: String) {
        if (noteText.isBlank()) notes.remove(planetId) else notes[planetId] = noteText
        notesFlow.value = notes.keys.toSet()
    }

    override suspend fun deleteNote(planetId: String) {
        notes.remove(planetId)
        notesFlow.value = notes.keys.toSet()
    }
}

class FakeFavouritesSyncRepository : FavouritesSyncRepository {
    override suspend fun runBackgroundSync(): Int = 0
    var immediateSyncCount = 0
    var backgroundScheduled = false

    override suspend fun syncStaleFavourites(ttlHours: Int): Int = 0

    override fun scheduleBackgroundSync() {
        backgroundScheduled = true
    }

    override fun scheduleImmediateSync() {
        immediateSyncCount++
    }

    override fun cancelBackgroundSync() = Unit
}

class FakePlanetUserStateRepository : PlanetUserStateRepository {
    private val states = mutableMapOf<String, PlanetUserState>()
    private val statesFlow = MutableStateFlow<Map<String, PlanetUserState>>(emptyMap())
    private val pinnedFlow = MutableStateFlow<List<String>>(emptyList())

    override fun observeAllStates(): Flow<Map<String, PlanetUserState>> = statesFlow

    override fun observePinnedPlanetIds(): Flow<List<String>> = pinnedFlow

    override suspend fun markAsRead(planetId: String) {
        val current = states.getOrPut(planetId) { emptyState(planetId) }
        states[planetId] = current.copy(isRead = true)
        statesFlow.value = states.toMap()
    }

    override suspend fun togglePin(planetId: String): PinResult {
        val current = states.getOrPut(planetId) { emptyState(planetId) }
        val result = if (current.isPinned) {
            states[planetId] = current.copy(isPinned = false, pinnedOrder = null)
            updatePinnedFlow()
            PinResult.Unpinned
        } else {
            val pinnedCount = states.values.count { it.isPinned }
            if (pinnedCount >= 5) {
                PinResult.LimitReached
            } else {
                states[planetId] = current.copy(isPinned = true, pinnedOrder = pinnedCount)
                updatePinnedFlow()
                PinResult.Pinned
            }
        }
        statesFlow.value = states.toMap()
        return result
    }

    override suspend fun setRating(planetId: String, rating: Int) {
        val current = states.getOrPut(planetId) { emptyState(planetId) }
        states[planetId] = current.copy(rating = rating.coerceIn(1, 5))
        statesFlow.value = states.toMap()
    }

    fun setPinnedIds(vararg ids: String) {
        ids.forEachIndexed { index, id ->
            val current = states.getOrPut(id) { emptyState(id) }
            states[id] = current.copy(isPinned = true, pinnedOrder = index)
        }
        updatePinnedFlow()
        statesFlow.value = states.toMap()
    }

    private fun emptyState(planetId: String) = PlanetUserState(
        planetId = planetId,
        isRead = false,
        isPinned = false,
        pinnedOrder = null,
        rating = null
    )

    private fun updatePinnedFlow() {
        pinnedFlow.value = states.values
            .filter { it.isPinned }
            .sortedBy { it.pinnedOrder }
            .map { it.planetId }
    }

    fun isRead(planetId: String): Boolean = states[planetId]?.isRead == true
}

class FakePlanetCollectionRepository : PlanetCollectionRepository {
    override fun observeCollections(): Flow<List<PlanetCollection>> = flowOf(emptyList())

    override fun observeCollectionItems(collectionId: String): Flow<List<PlanetCollectionItem>> =
        flowOf(emptyList())

    override fun observePlanetCollectionIds(planetId: String): Flow<Set<String>> = flowOf(emptySet())

    override suspend fun createCollection(name: String): String = "col-1"

    override suspend fun deleteCollection(collectionId: String) = Unit

    override suspend fun addPlanetToCollection(collectionId: String, planetId: String, planetName: String) =
        Unit

    override suspend fun removePlanetFromCollection(collectionId: String, planetId: String) = Unit

    override suspend fun togglePlanetInCollection(collectionId: String, planetId: String, planetName: String) =
        Unit
}
