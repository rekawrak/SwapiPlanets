package com.example.swapiplanets.data.repository

import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.PlanetCacheRepository
import com.example.swapiplanets.domain.repository.PlanetRemoteDataSource
import com.example.swapiplanets.domain.repository.PlanetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstPlanetRepository @Inject constructor(
    private val remote: PlanetRemoteDataSource,
    private val cache: PlanetCacheRepository
) : PlanetRepository {

    override suspend fun getPlanets(page: Int): List<Planet> {
        return try {
            val planets = remote.fetchPlanets(page)
            cache.saveAll(planets)
            planets
        } catch (e: Exception) {
            val cached = cache.getAll()
            if (cached.isNotEmpty()) cached else throw e
        }
    }

    override suspend fun getPlanetDetail(id: String): Planet {
        return try {
            val planet = remote.fetchPlanetDetail(id)
            cache.save(planet)
            planet
        } catch (e: Exception) {
            cache.get(id) ?: throw e
        }
    }

    override suspend fun getCachedPlanets(): List<Planet> = cache.getAll()

    override suspend fun getCachedPlanetDetail(id: String): Planet? = cache.get(id)
}
