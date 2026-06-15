package com.example.swapiplanets.domain.repository

import kotlinx.coroutines.flow.Flow

interface PlanetUserPreferencesRepository {
    fun observeListOnlyFavourites(): Flow<Boolean>
    suspend fun setListOnlyFavourites(enabled: Boolean)

    fun observeSortNamesDescending(): Flow<Boolean>
    suspend fun setSortNamesDescending(descending: Boolean)

    fun observeCacheTtlHours(): Flow<Int>
    suspend fun setCacheTtlHours(hours: Int)

    fun observeBackgroundRefreshEnabled(): Flow<Boolean>
    suspend fun setBackgroundRefreshEnabled(enabled: Boolean)
}
