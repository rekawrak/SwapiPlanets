package com.example.swapiplanets.data.repository

import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.PlanetCacheRepository
import com.example.swapiplanets.domain.repository.PlanetRemoteDataSource
import com.example.swapiplanets.testutil.planet
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineFirstPlanetRepositoryTest {

    private class FakeRemote : PlanetRemoteDataSource {
        var planetsResult: Result<List<Planet>> = Result.success(emptyList())
        var detailResults: MutableMap<String, Result<Planet>> = mutableMapOf()

        override suspend fun fetchPlanets(page: Int): List<Planet> = planetsResult.getOrThrow()

        override suspend fun fetchPlanetDetail(id: String): Planet =
            detailResults[id]?.getOrThrow() ?: error("No stub for id=$id")
    }

    private class FakeCache : PlanetCacheRepository {
        val stored = mutableMapOf<String, Planet>()

        override suspend fun save(planet: Planet) {
            stored[planet.id] = planet
        }

        override suspend fun saveAll(planets: List<Planet>) {
            planets.forEach { stored[it.id] = it }
        }

        override suspend fun get(id: String) = stored[id]

        override suspend fun getAll() = stored.values.sortedBy { it.name }

        override suspend fun getByIds(ids: List<String>) = ids.mapNotNull { stored[it] }

        override fun isStale(cachedAtMs: Long, ttlHours: Int) = false
    }

    @Test
    fun getPlanets_savesNetworkResultToCache() = runTest {
        val remote = FakeRemote().apply {
            planetsResult = Result.success(listOf(planet("1", "Tatooine")))
        }
        val cache = FakeCache()
        val repo = OfflineFirstPlanetRepository(remote, cache)

        val result = repo.getPlanets()

        assertEquals("Tatooine", result.first().name)
        assertEquals("Tatooine", cache.get("1")?.name)
    }

    @Test
    fun getPlanets_returnsCacheWhenNetworkFails() = runTest {
        val remote = FakeRemote().apply {
            planetsResult = Result.failure(RuntimeException("offline"))
        }
        val cache = FakeCache().apply {
            saveAll(listOf(planet("1", "Cached")))
        }
        val repo = OfflineFirstPlanetRepository(remote, cache)

        val result = repo.getPlanets()

        assertEquals("Cached", result.first().name)
    }

    @Test
    fun getPlanetDetail_returnsCachedDetailWhenNetworkFails() = runTest {
        val remote = FakeRemote().apply {
            detailResults["5"] = Result.failure(RuntimeException("offline"))
        }
        val cache = FakeCache().apply {
            save(planet("5", "Dagobah"))
        }
        val repo = OfflineFirstPlanetRepository(remote, cache)

        val result = repo.getPlanetDetail("5")

        assertEquals("Dagobah", result.name)
    }

    @Test
    fun getPlanets_rethrowsWhenNetworkFailsAndCacheEmpty() = runTest {
        val remote = FakeRemote().apply {
            planetsResult = Result.failure(RuntimeException("offline"))
        }
        val repo = OfflineFirstPlanetRepository(remote, FakeCache())

        val error = runCatching { repo.getPlanets() }.exceptionOrNull()
        assertTrue(error is RuntimeException)
    }
}
