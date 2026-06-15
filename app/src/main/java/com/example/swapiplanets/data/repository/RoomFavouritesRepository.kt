package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.local.FavouritePlanetEntity
import com.example.swapiplanets.data.local.FavouritesDao
import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomFavouritesRepository @Inject constructor(
    private val dao: FavouritesDao,
    private val userProfileRepository: UserProfileRepository
) : FavouritesRepository {

    override fun observeAll(): Flow<Set<String>> {
        return userProfileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observeAll(profileId).map { entities -> entities.map { it.planetId }.toSet() }
        }
    }

    override suspend fun getAll(): Set<String> {
        val profileId = userProfileRepository.getActiveProfileId()
        return dao.getAll(profileId).map { it.planetId }.toSet()
    }

    suspend fun getAllEntries(): List<FavouritePlanetEntity> {
        val profileId = userProfileRepository.getActiveProfileId()
        return dao.getAll(profileId)
    }

    override suspend fun isFavourite(id: String): Boolean {
        val profileId = userProfileRepository.getActiveProfileId()
        return dao.isFavourite(profileId, id)
    }

    override suspend fun toggle(id: String) {
        val profileId = userProfileRepository.getActiveProfileId()
        val now = System.currentTimeMillis()
        if (dao.isFavourite(profileId, id)) {
            dao.delete(
                FavouritePlanetEntity(
                    profileId = profileId,
                    planetId = id,
                    addedAtMs = 0,
                    lastSyncedAtMs = null
                )
            )
        } else {
            dao.insert(
                FavouritePlanetEntity(
                    profileId = profileId,
                    planetId = id,
                    addedAtMs = now,
                    lastSyncedAtMs = null
                )
            )
        }
    }

    suspend fun markSynced(planetId: String, syncedAtMs: Long) {
        val profileId = userProfileRepository.getActiveProfileId()
        dao.updateLastSynced(profileId, planetId, syncedAtMs)
    }
}
