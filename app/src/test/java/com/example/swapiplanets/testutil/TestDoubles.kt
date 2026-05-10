package com.example.swapiplanets.testutil

import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.PlanetRepository
import com.example.swapiplanets.domain.repository.PlanetUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePlanetRepository : PlanetRepository {
    var planetsResult: Result<List<Planet>> = Result.success(emptyList())
    var detailResults: MutableMap<String, Result<Planet>> = mutableMapOf()
    var planetsRequestCount: Int = 0
    var detailRequestIds: MutableList<String> = mutableListOf()

    override suspend fun getPlanets(page: Int): List<Planet> {
        planetsRequestCount += 1
        return planetsResult.getOrThrow()
    }

    override suspend fun getPlanetDetail(id: String): Planet {
        detailRequestIds.add(id)
        return detailResults[id]?.getOrThrow()
            ?: error("No detail stub configured for id=$id")
    }
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
    sortNamesDescending: Boolean = false
) : PlanetUserPreferencesRepository {
    private val onlyFavouritesFlow = MutableStateFlow(onlyFavourites)
    private val sortDescendingFlow = MutableStateFlow(sortNamesDescending)

    override fun observeListOnlyFavourites(): Flow<Boolean> = onlyFavouritesFlow

    override suspend fun setListOnlyFavourites(enabled: Boolean) {
        onlyFavouritesFlow.value = enabled
    }

    override fun observeSortNamesDescending(): Flow<Boolean> = sortDescendingFlow

    override suspend fun setSortNamesDescending(descending: Boolean) {
        sortDescendingFlow.value = descending
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
