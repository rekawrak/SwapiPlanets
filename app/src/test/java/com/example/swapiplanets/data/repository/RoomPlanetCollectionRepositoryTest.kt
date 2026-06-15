package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.local.PlanetCollectionDao
import com.example.swapiplanets.data.local.PlanetCollectionEntity
import com.example.swapiplanets.data.local.PlanetCollectionItemEntity
import com.example.swapiplanets.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class RoomPlanetCollectionRepositoryTest {

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

    private class InMemoryPlanetCollectionDao : PlanetCollectionDao {
        val collections = mutableMapOf<String, PlanetCollectionEntity>()
        val items = mutableListOf<PlanetCollectionItemEntity>()

        override fun observeByProfile(profileId: String): Flow<List<PlanetCollectionEntity>> {
            return flowOf(collections.values.filter { it.profileId == profileId })
        }

        override fun observeItems(collectionId: String): Flow<List<PlanetCollectionItemEntity>> {
            return flowOf(items.filter { it.collectionId == collectionId })
        }

        override fun observeItemsForPlanet(profileId: String, planetId: String): Flow<List<PlanetCollectionItemEntity>> {
            return flowOf(items.filter { it.profileId == profileId && it.planetId == planetId })
        }

        override fun observeItemCount(collectionId: String): Flow<Int> {
            return flowOf(items.count { it.collectionId == collectionId })
        }

        override suspend fun getById(collectionId: String): PlanetCollectionEntity? {
            return collections[collectionId]
        }

        override suspend fun insert(entity: PlanetCollectionEntity) {
            collections[entity.collectionId] = entity
        }

        override suspend fun delete(collectionId: String) {
            collections.remove(collectionId)
            items.removeAll { it.collectionId == collectionId }
        }

        override suspend fun insertItem(entity: PlanetCollectionItemEntity) {
            items.add(entity)
        }

        override suspend fun deleteItem(collectionId: String, planetId: String) {
            items.removeAll { it.collectionId == collectionId && it.planetId == planetId }
        }

        override suspend fun isPlanetInCollection(collectionId: String, planetId: String): Boolean {
            return items.any { it.collectionId == collectionId && it.planetId == planetId }
        }

        override suspend fun deleteByProfileId(profileId: String) {
            val idsToRemove = collections.values.filter { it.profileId == profileId }.map { it.collectionId }
            idsToRemove.forEach { collections.remove(it) }
            items.removeAll { it.profileId == profileId }
        }
    }

    @Test
    fun createCollection_insertsIntoDao() = runTest {
        val dao = InMemoryPlanetCollectionDao()
        val repo = RoomPlanetCollectionRepository(dao, FakeProfileRepo())

        val id = repo.createCollection("My Planets")
        assertEquals(1, dao.collections.size)
        assertEquals("My Planets", dao.collections[id]?.name)
    }

    @Test
    fun addPlanetToCollection_insertsItem() = runTest {
        val dao = InMemoryPlanetCollectionDao()
        val repo = RoomPlanetCollectionRepository(dao, FakeProfileRepo())

        val colId = repo.createCollection("Favs")
        repo.addPlanetToCollection(colId, "1", "Tatooine")

        assertEquals(1, dao.items.size)
        assertEquals("1", dao.items.first().planetId)
    }

    @Test
    fun togglePlanetInCollection_addsThenRemoves() = runTest {
        val dao = InMemoryPlanetCollectionDao()
        val repo = RoomPlanetCollectionRepository(dao, FakeProfileRepo())

        val colId = repo.createCollection("Favs")
        
        repo.togglePlanetInCollection(colId, "1", "Tatooine")
        assertTrue(dao.isPlanetInCollection(colId, "1"))
        
        repo.togglePlanetInCollection(colId, "1", "Tatooine")
        assertTrue(!dao.isPlanetInCollection(colId, "1"))
    }
}
