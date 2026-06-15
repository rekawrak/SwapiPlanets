package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.local.PlanetVisitEntity
import com.example.swapiplanets.data.local.VisitHistoryDao
import com.example.swapiplanets.data.mapper.toDomain
import com.example.swapiplanets.domain.model.RecentVisit
import com.example.swapiplanets.domain.repository.UserProfileRepository
import com.example.swapiplanets.domain.repository.VisitHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomVisitHistoryRepository @Inject constructor(
    private val dao: VisitHistoryDao,
    private val userProfileRepository: UserProfileRepository
) : VisitHistoryRepository {

    override suspend fun recordVisit(planetId: String, planetName: String) {
        val profileId = userProfileRepository.getActiveProfileId()
        dao.upsert(
            PlanetVisitEntity(
                profileId = profileId,
                planetId = planetId,
                planetName = planetName,
                visitedAtMs = System.currentTimeMillis()
            )
        )
    }

    override fun observeRecent(limit: Int): Flow<List<RecentVisit>> {
        return userProfileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observeRecent(profileId, limit).map { entities -> entities.map { it.toDomain() } }
        }
    }
}
