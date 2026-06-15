package com.example.swapiplanets.data.repository

import com.example.swapiplanets.testutil.planet
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomPlanetCacheRepositoryTest {

    private class InMemoryPlanetCacheDao : com.example.swapiplanets.data.local.PlanetCacheDao {
        private val data = linkedMapOf<String, com.example.swapiplanets.data.local.CachedPlanetEntity>()

        override suspend fun getAll() = data.values.sortedBy { it.name }

        override suspend fun getById(id: String) = data[id]

        override suspend fun getByIds(ids: List<String>) = ids.mapNotNull { data[it] }

        override suspend fun upsert(entity: com.example.swapiplanets.data.local.CachedPlanetEntity) {
            data[entity.planetId] = entity
        }

        override suspend fun upsertAll(entities: List<com.example.swapiplanets.data.local.CachedPlanetEntity>) {
            entities.forEach { data[it.planetId] = it }
        }
    }

    @Test
    fun isStale_returnsTrueWhenOlderThanTtl() {
        val repo = RoomPlanetCacheRepository(InMemoryPlanetCacheDao())
        val old = System.currentTimeMillis() - (25L * 60L * 60L * 1000L)
        assertTrue(repo.isStale(old, ttlHours = 24))
    }

    @Test
    fun saveAndGet_roundTrip() = runTest {
        val dao = InMemoryPlanetCacheDao()
        val repo = RoomPlanetCacheRepository(dao)
        val p = planet("1", "Tatooine")
        repo.save(p)
        assertEquals("Tatooine", repo.get("1")?.name)
    }
}
