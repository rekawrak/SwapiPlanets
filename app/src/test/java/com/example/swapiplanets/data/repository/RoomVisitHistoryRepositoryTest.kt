package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.local.PlanetVisitEntity
import com.example.swapiplanets.data.local.VisitHistoryDao
import com.example.swapiplanets.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomVisitHistoryRepositoryTest {

    private class FakeProfileRepo(private val profileId: String = "p1") : UserProfileRepository {
        override fun observeProfiles() = flowOf(emptyList<com.example.swapiplanets.domain.model.UserProfile>())
        override fun observeActiveProfileId() = flowOf(profileId)
        override suspend fun ensureDefaultProfile() = Unit
        override suspend fun getActiveProfileId() = profileId
        override suspend fun createProfile(displayName: String) =
            com.example.swapiplanets.domain.repository.CreateProfileResult.Created("new")
        override suspend fun deleteProfile(profileId: String) =
            com.example.swapiplanets.domain.repository.DeleteProfileResult.Deleted
        override suspend fun switchProfile(profileId: String) = Unit
    }

    private class InMemoryVisitHistoryDao : VisitHistoryDao {
        val visits = mutableMapOf<Pair<String, String>, PlanetVisitEntity>()

        override fun observeRecent(profileId: String, limit: Int): Flow<List<PlanetVisitEntity>> {
            val list = visits.values
                .filter { it.profileId == profileId }
                .sortedByDescending { it.visitedAtMs }
                .take(limit)
            return flowOf(list)
        }

        override suspend fun getRecent(profileId: String, limit: Int): List<PlanetVisitEntity> {
            return visits.values
                .filter { it.profileId == profileId }
                .sortedByDescending { it.visitedAtMs }
                .take(limit)
        }

        override suspend fun upsert(entity: PlanetVisitEntity) {
            visits[Pair(entity.profileId, entity.planetId)] = entity
        }

        override suspend fun deleteByProfileId(profileId: String) {
            val keysToRemove = visits.keys.filter { it.first == profileId }
            keysToRemove.forEach { visits.remove(it) }
        }
    }

    @Test
    fun recordVisit_savesToDao() = runTest {
        val dao = InMemoryVisitHistoryDao()
        val repo = RoomVisitHistoryRepository(dao, FakeProfileRepo())

        repo.recordVisit("1", "Tatooine")
        
        assertEquals(1, dao.visits.size)
        val entity = dao.visits[Pair("p1", "1")]
        assertEquals("Tatooine", entity?.planetName)
    }
}
