package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.mapper.toDomain
import com.example.swapiplanets.data.network.SwapiApi
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.PlanetRepository
import javax.inject.Inject

class PlanetRepositoryImpl @Inject constructor(
    private val api: SwapiApi
) : PlanetRepository {

    override suspend fun getPlanets(page: Int): List<Planet> {
        return api.getPlanets(page).results.map { it.toDomain() }
    }

    override suspend fun getPlanetDetail(id: String): Planet {
        return api.getPlanetDetail(id).toDomain()
    }
}