package com.example.swapiplanets.domain.repository

import com.example.swapiplanets.domain.model.PlanetCollection
import com.example.swapiplanets.domain.model.PlanetCollectionItem
import kotlinx.coroutines.flow.Flow

interface PlanetCollectionRepository {
    fun observeCollections(): Flow<List<PlanetCollection>>
    fun observeCollectionItems(collectionId: String): Flow<List<PlanetCollectionItem>>
    fun observePlanetCollectionIds(planetId: String): Flow<Set<String>>
    suspend fun createCollection(name: String): String
    suspend fun deleteCollection(collectionId: String)
    suspend fun addPlanetToCollection(collectionId: String, planetId: String, planetName: String)
    suspend fun removePlanetFromCollection(collectionId: String, planetId: String)
    suspend fun togglePlanetInCollection(collectionId: String, planetId: String, planetName: String)
}

interface PlanetUserStateRepository {
    fun observeAllStates(): Flow<Map<String, com.example.swapiplanets.domain.model.PlanetUserState>>
    fun observePinnedPlanetIds(): Flow<List<String>>
    suspend fun markAsRead(planetId: String)
    suspend fun togglePin(planetId: String): PinResult
    suspend fun setRating(planetId: String, rating: Int)
}

sealed class PinResult {
    data object Pinned : PinResult()
    data object Unpinned : PinResult()
    data object LimitReached : PinResult()
}
