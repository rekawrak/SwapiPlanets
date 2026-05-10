package com.example.swapiplanets.domain.repository

import kotlinx.coroutines.flow.Flow


interface PlanetUserPreferencesRepository {
    fun observeListOnlyFavourites(): Flow<Boolean>
    suspend fun setListOnlyFavourites(enabled: Boolean)

    fun observeSortNamesDescending(): Flow<Boolean>
    suspend fun setSortNamesDescending(descending: Boolean)
}
