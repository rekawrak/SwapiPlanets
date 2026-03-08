package com.example.swapiplanets.data.network

import com.example.swapiplanets.data.dto.PlanetDto
import com.example.swapiplanets.data.dto.PlanetListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SwapiApi {

    @GET("planets/")
    suspend fun getPlanets(
        @Query("page") page: Int = 1
    ): PlanetListResponseDto

    @GET("planets/{id}/")
    suspend fun getPlanetDetail(
        @Path("id") id: String
    ): PlanetDto
}