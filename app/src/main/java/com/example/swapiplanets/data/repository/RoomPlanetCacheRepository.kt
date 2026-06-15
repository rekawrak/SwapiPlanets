package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.local.PlanetCacheDao
import com.example.swapiplanets.data.mapper.toCachedEntity
import com.example.swapiplanets.data.mapper.toDomain
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.PlanetCacheRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomPlanetCacheRepository @Inject constructor(
    private val dao: PlanetCacheDao
) : PlanetCacheRepository {

    override suspend fun save(planet: Planet) {
        dao.upsert(planet.toCachedEntity(System.currentTimeMillis()))
    }

    override suspend fun saveAll(planets: List<Planet>) {
        val now = System.currentTimeMillis()
        dao.upsertAll(planets.map { it.toCachedEntity(now) })
    }

    override suspend fun get(id: String): Planet? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun getAll(): List<Planet> {
        return dao.getAll().map { it.toDomain() }
    }

    override suspend fun getByIds(ids: List<String>): List<Planet> {
        if (ids.isEmpty()) return emptyList()
        return dao.getByIds(ids).map { it.toDomain() }
    }

    override fun isStale(cachedAtMs: Long, ttlHours: Int): Boolean {
        val ttlMs = ttlHours.coerceAtLeast(1) * 60L * 60L * 1000L
        return System.currentTimeMillis() - cachedAtMs > ttlMs
    }
}
