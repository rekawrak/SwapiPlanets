package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.local.PlanetUserStateDao
import com.example.swapiplanets.data.local.PlanetUserStateEntity
import com.example.swapiplanets.domain.model.PlanetUserState
import com.example.swapiplanets.domain.repository.PinResult
import com.example.swapiplanets.domain.repository.PlanetUserStateRepository
import com.example.swapiplanets.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomPlanetUserStateRepository @Inject constructor(
    private val dao: PlanetUserStateDao,
    private val userProfileRepository: UserProfileRepository
) : PlanetUserStateRepository {

    override fun observeAllStates(): Flow<Map<String, PlanetUserState>> {
        return userProfileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observeAll(profileId).map { entities ->
                entities.associate { entity ->
                    entity.planetId to PlanetUserState(
                        planetId = entity.planetId,
                        isRead = entity.isRead,
                        isPinned = entity.isPinned,
                        pinnedOrder = entity.pinnedOrder,
                        rating = entity.rating
                    )
                }
            }
        }
    }

    override fun observePinnedPlanetIds(): Flow<List<String>> {
        return userProfileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observePinnedPlanetIds(profileId)
        }
    }

    override suspend fun markAsRead(planetId: String) {
        val profileId = userProfileRepository.getActiveProfileId()
        val existing = dao.get(profileId, planetId)
        dao.upsert(
            (existing ?: PlanetUserStateEntity(profileId = profileId, planetId = planetId)).copy(
                isRead = true,
                readAtMs = System.currentTimeMillis()
            )
        )
    }

    override suspend fun togglePin(planetId: String): PinResult {
        val profileId = userProfileRepository.getActiveProfileId()
        val existing = dao.get(profileId, planetId)
        if (existing?.isPinned == true) {
            dao.upsert(existing.copy(isPinned = false, pinnedOrder = null))
            reindexPinned(profileId)
            return PinResult.Unpinned
        }
        if (dao.pinnedCount(profileId) >= MAX_PINNED) {
            return PinResult.LimitReached
        }
        val nextOrder = dao.pinnedCount(profileId)
        dao.upsert(
            (existing ?: PlanetUserStateEntity(profileId = profileId, planetId = planetId)).copy(
                isPinned = true,
                pinnedOrder = nextOrder
            )
        )
        return PinResult.Pinned
    }

    override suspend fun setRating(planetId: String, rating: Int) {
        val profileId = userProfileRepository.getActiveProfileId()
        val clamped = rating.coerceIn(1, 5)
        val existing = dao.get(profileId, planetId)
        dao.upsert(
            (existing ?: PlanetUserStateEntity(profileId = profileId, planetId = planetId)).copy(
                rating = clamped
            )
        )
    }

    private suspend fun reindexPinned(profileId: String) {
        dao.getPinnedPlanetIds(profileId).forEachIndexed { index, id ->
            val entity = dao.get(profileId, id) ?: return@forEachIndexed
            dao.upsert(entity.copy(pinnedOrder = index))
        }
    }

    companion object {
        const val MAX_PINNED = 5
    }
}
