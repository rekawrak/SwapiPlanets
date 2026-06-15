package com.example.swapiplanets.data.repository

import com.example.swapiplanets.domain.repository.PinResult
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomPlanetUserStateRepositoryTest {

    private class FakeProfileRepo(private val profileId: String = "p1") :
        com.example.swapiplanets.domain.repository.UserProfileRepository {
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

    private class InMemoryUserStateDao : com.example.swapiplanets.data.local.PlanetUserStateDao {
        val states = mutableMapOf<Pair<String, String>, com.example.swapiplanets.data.local.PlanetUserStateEntity>()

        override fun observeAll(profileId: String) = flowOf(
            states.values.filter { it.profileId == profileId }
        )

        override suspend fun get(profileId: String, planetId: String) = states[profileId to planetId]

        override suspend fun upsert(entity: com.example.swapiplanets.data.local.PlanetUserStateEntity) {
            states[entity.profileId to entity.planetId] = entity
        }

        override fun observePinnedPlanetIds(profileId: String) = flowOf(
            states.values.filter { it.profileId == profileId && it.isPinned }
                .sortedBy { it.pinnedOrder }
                .map { it.planetId }
        )

        override suspend fun pinnedCount(profileId: String) =
            states.values.count { it.profileId == profileId && it.isPinned }

        override suspend fun getPinnedPlanetIds(profileId: String) =
            states.values.filter { it.profileId == profileId && it.isPinned }
                .sortedBy { it.pinnedOrder }
                .map { it.planetId }
        
        override suspend fun deleteByProfileId(profileId: String) {
            val keysToRemove = states.keys.filter { it.first == profileId }
            keysToRemove.forEach { states.remove(it) }
        }
    }

    @Test
    fun togglePin_limitsToFivePlanets() = runTest {
        val dao = InMemoryUserStateDao()
        val repo = RoomPlanetUserStateRepository(dao, FakeProfileRepo())

        repeat(5) { index ->
            assertEquals(PinResult.Pinned, repo.togglePin("planet-$index"))
        }
        assertEquals(PinResult.LimitReached, repo.togglePin("planet-6"))
        assertEquals(5, dao.pinnedCount("p1"))
    }

    @Test
    fun markAsRead_setsReadFlag() = runTest {
        val dao = InMemoryUserStateDao()
        val repo = RoomPlanetUserStateRepository(dao, FakeProfileRepo())
        repo.markAsRead("1")
        assertTrue(dao.get("p1", "1")?.isRead == true)
    }

    @Test
    fun setRating_clampsToFive() = runTest {
        val dao = InMemoryUserStateDao()
        val repo = RoomPlanetUserStateRepository(dao, FakeProfileRepo())
        repo.setRating("1", 10)
        assertEquals(5, dao.get("p1", "1")?.rating)
    }
}
