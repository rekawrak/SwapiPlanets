package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.mapper.toDomain
import com.example.swapiplanets.data.network.SwapiApi
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.repository.PlanetRemoteDataSource
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class RemotePlanetDataSource @Inject constructor(
    @Named("primary") private val primaryApi: SwapiApi,
    @Named("fallback") private val fallbackApi: SwapiApi
) : PlanetRemoteDataSource {
    override suspend fun fetchPlanets(page: Int): List<Planet> {
        return runCatching {
            primaryApi.getPlanets(page).results
        }.getOrElse {
            fallbackApi.getPlanets(page).results
        }.map { it.toDomain() }
    }

    override suspend fun fetchPlanetDetail(id: String): Planet {
        return runCatching {
            primaryApi.getPlanetDetail(id)
        }.getOrElse {
            fallbackApi.getPlanetDetail(id)
        }.toDomain()
    }
}