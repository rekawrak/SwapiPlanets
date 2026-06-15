package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.local.PlanetNoteEntity
import com.example.swapiplanets.data.local.PlanetNotesDao
import com.example.swapiplanets.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomPlanetNotesRepositoryTest {

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

    private class InMemoryPlanetNotesDao : PlanetNotesDao {
        val notes = mutableMapOf<Pair<String, String>, PlanetNoteEntity>()

        override fun observeByPlanetId(profileId: String, planetId: String): Flow<PlanetNoteEntity?> {
            return flowOf(notes[Pair(profileId, planetId)])
        }

        override fun observeAll(profileId: String): Flow<List<PlanetNoteEntity>> {
            return flowOf(notes.values.filter { it.profileId == profileId }.sortedByDescending { it.updatedAtMs })
        }

        override fun observePlanetIdsWithNotes(profileId: String): Flow<List<String>> {
            return flowOf(notes.values.filter { it.profileId == profileId }.map { it.planetId })
        }

        override suspend fun upsert(entity: PlanetNoteEntity) {
            notes[Pair(entity.profileId, entity.planetId)] = entity
        }

        override suspend fun delete(profileId: String, planetId: String) {
            notes.remove(Pair(profileId, planetId))
        }

        override suspend fun deleteByProfileId(profileId: String) {
            val keysToRemove = notes.keys.filter { it.first == profileId }
            keysToRemove.forEach { notes.remove(it) }
        }
    }

    @Test
    fun saveNote_insertsIntoDao() = runTest {
        val dao = InMemoryPlanetNotesDao()
        val repo = RoomPlanetNotesRepository(dao, FakeProfileRepo())

        repo.saveNote("1", "Tatooine", "Hot place")
        
        assertEquals(1, dao.notes.size)
        assertEquals("Hot place", dao.notes[Pair("p1", "1")]?.noteText)
    }

    @Test
    fun saveNote_withEmptyText_deletesFromDao() = runTest {
        val dao = InMemoryPlanetNotesDao()
        val repo = RoomPlanetNotesRepository(dao, FakeProfileRepo())

        repo.saveNote("1", "Tatooine", "Hot place")
        repo.saveNote("1", "Tatooine", "   ")
        
        assertEquals(0, dao.notes.size)
    }

    @Test
    fun deleteNote_removesFromDao() = runTest {
        val dao = InMemoryPlanetNotesDao()
        val repo = RoomPlanetNotesRepository(dao, FakeProfileRepo())

        repo.saveNote("1", "Tatooine", "Hot place")
        repo.deleteNote("1")
        
        assertEquals(0, dao.notes.size)
    }
}
