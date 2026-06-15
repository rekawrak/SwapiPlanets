package com.example.swapiplanets.domain.repository

import com.example.swapiplanets.domain.model.Planet

interface PlanetRepository {
    suspend fun getPlanets(page: Int = 1): List<Planet>
    suspend fun getPlanetDetail(id: String): Planet
    suspend fun getCachedPlanets(): List<Planet>
    suspend fun getCachedPlanetDetail(id: String): Planet?
}
