package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.mapper.toDomain
import com.example.swapiplanets.data.network.SwapiApi
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.PlanetRepository
import javax.inject.Inject
import javax.inject.Named

class PlanetRepositoryImpl @Inject constructor(
    @Named("primary") private val primaryApi: SwapiApi,
    @Named("fallback") private val fallbackApi: SwapiApi
) : PlanetRepository {

    override suspend fun getPlanets(page: Int): List<Planet> {
        return runCatching {
            primaryApi.getPlanets(page).results
        }.getOrElse {
            fallbackApi.getPlanets(page).results
        }.map { it.toDomain() }
    }

    override suspend fun getPlanetDetail(id: String): Planet {
        return runCatching {
            primaryApi.getPlanetDetail(id)
        }.getOrElse {
            fallbackApi.getPlanetDetail(id)
        }.toDomain()
    }
}