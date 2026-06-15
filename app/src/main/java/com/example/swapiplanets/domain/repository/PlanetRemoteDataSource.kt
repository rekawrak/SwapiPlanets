package com.example.swapiplanets.domain.repository

import com.example.swapiplanets.domain.model.Planet

interface PlanetRemoteDataSource {
    suspend fun fetchPlanets(page: Int): List<Planet>
    suspend fun fetchPlanetDetail(id: String): Planet
}
