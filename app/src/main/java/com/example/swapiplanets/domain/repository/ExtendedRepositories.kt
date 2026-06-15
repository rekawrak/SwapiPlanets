package com.example.swapiplanets.domain.repository

import com.example.swapiplanets.domain.model.Planet
import kotlinx.coroutines.flow.Flow

interface PlanetCacheRepository {
    suspend fun save(planet: Planet)
    suspend fun saveAll(planets: List<Planet>)
    suspend fun get(id: String): Planet?
    suspend fun getAll(): List<Planet>
    suspend fun getByIds(ids: List<String>): List<Planet>
    fun isStale(cachedAtMs: Long, ttlHours: Int): Boolean
}

interface VisitHistoryRepository {
    suspend fun recordVisit(planetId: String, planetName: String)
    fun observeRecent(limit: Int = 20): Flow<List<com.example.swapiplanets.domain.model.RecentVisit>>
}

interface PlanetNotesRepository {
    fun observeNote(planetId: String): Flow<String?>
    fun observeAllNotes(): Flow<List<com.example.swapiplanets.domain.model.PlanetNote>>
    fun observePlanetIdsWithNotes(): Flow<Set<String>>
    suspend fun saveNote(planetId: String, planetName: String, noteText: String)
    suspend fun deleteNote(planetId: String)
}

interface FavouritesSyncRepository {
    suspend fun syncStaleFavourites(ttlHours: Int): Int
    fun scheduleBackgroundSync()
    fun scheduleImmediateSync()
    fun cancelBackgroundSync()
    suspend fun runBackgroundSync(): Int
}