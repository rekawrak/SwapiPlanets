package com.example.swapiplanets.data.sync

import com.example.swapiplanets.data.local.FavouritePlanetEntity
import com.example.swapiplanets.data.repository.RoomFavouritesRepository
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.model.UserProfile
import com.example.swapiplanets.domain.repository.CreateProfileResult
import com.example.swapiplanets.domain.repository.DeleteProfileResult
import com.example.swapiplanets.domain.repository.FavouritesSyncScheduler
import com.example.swapiplanets.domain.repository.PlanetCacheRepository
import com.example.swapiplanets.domain.repository.PlanetRemoteDataSource
import com.example.swapiplanets.domain.repository.PlanetUserPreferencesRepository
import com.example.swapiplanets.domain.repository.UserProfileRepository
import com.example.swapiplanets.testutil.planet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavouritesSyncRepositoryImplTest {

    private class FakeRemote : PlanetRemoteDataSource {
        val fetchedIds = mutableListOf<String>()
        var detailById: Map<String, Planet> = emptyMap()

        override suspend fun fetchPlanets(page: Int): List<Planet> = emptyList()

        override suspend fun fetchPlanetDetail(id: String): Planet {
            fetchedIds.add(id)
            return detailById[id] ?: error("Missing planet $id")
        }
    }

    private class FakeCache : PlanetCacheRepository {
        val saved = mutableListOf<Planet>()

        override suspend fun save(planet: Planet) {
            saved.add(planet)
        }

        override suspend fun saveAll(planets: List<Planet>) { saved.addAll(planets) }

        override suspend fun get(id: String) = saved.lastOrNull { it.id == id }

        override suspend fun getAll() = saved

        override suspend fun getByIds(ids: List<String>) = saved.filter { it.id in ids }

        override fun isStale(cachedAtMs: Long, ttlHours: Int) = false
    }

    private class FakeFavouritesDao : com.example.swapiplanets.data.local.FavouritesDao {
        val entries = mutableListOf<FavouritePlanetEntity>()

        override suspend fun getAll(profileId: String) =
            entries.filter { it.profileId == profileId }

        override fun observeAll(profileId: String): Flow<List<FavouritePlanetEntity>> =
            flowOf(entries.filter { it.profileId == profileId })

        override suspend fun isFavourite(profileId: String, planetId: String) =
            entries.any { it.profileId == profileId && it.planetId == planetId }

        override suspend fun insert(entity: FavouritePlanetEntity): Long {
            entries.removeAll { it.profileId == entity.profileId && it.planetId == entity.planetId }
            entries.add(entity)
            return 1L
        }

        override suspend fun delete(entity: FavouritePlanetEntity) {
            entries.removeAll { it.profileId == entity.profileId && it.planetId == entity.planetId }
        }

        override suspend fun updateLastSynced(profileId: String, planetId: String, syncedAtMs: Long) {
            val index = entries.indexOfFirst { it.profileId == profileId && it.planetId == planetId }
            if (index >= 0) entries[index] = entries[index].copy(lastSyncedAtMs = syncedAtMs)
        }

        override suspend fun deleteByProfileId(profileId: String) {
            entries.removeAll { it.profileId == profileId }
        }
    }

    private class FakeProfileRepo : UserProfileRepository {
        override fun observeProfiles(): Flow<List<UserProfile>> = flowOf(emptyList())
        override fun observeActiveProfileId(): Flow<String> = flowOf("p1")
        override suspend fun ensureDefaultProfile() = Unit
        override suspend fun getActiveProfileId() = "p1"
        override suspend fun createProfile(displayName: String) = CreateProfileResult.Created("x")
        override suspend fun deleteProfile(profileId: String) = DeleteProfileResult.Deleted
        override suspend fun switchProfile(profileId: String) = Unit
    }

    private class FakePreferences(
        private val ttlHours: Int = 24,
        private val backgroundEnabled: Boolean = true
    ) : PlanetUserPreferencesRepository {
        override fun observeListOnlyFavourites(): Flow<Boolean> = flowOf(false)
        override suspend fun setListOnlyFavourites(enabled: Boolean) = Unit
        override fun observeSortNamesDescending(): Flow<Boolean> = flowOf(false)
        override suspend fun setSortNamesDescending(descending: Boolean) = Unit
        override fun observeCacheTtlHours(): Flow<Int> = flowOf(ttlHours)
        override suspend fun setCacheTtlHours(hours: Int) = Unit
        override fun observeBackgroundRefreshEnabled(): Flow<Boolean> = flowOf(backgroundEnabled)
        override suspend fun setBackgroundRefreshEnabled(enabled: Boolean) = Unit
    }

    private class RecordingScheduler : FavouritesSyncScheduler {
        var periodicScheduled = false
        var oneTimeScheduled = false

        override fun schedulePeriodicSync() {
            periodicScheduled = true
        }

        override fun scheduleOneTimeSync() {
            oneTimeScheduled = true
        }

        override fun cancelPeriodicSync() = Unit
    }

    private lateinit var dao: FakeFavouritesDao
    private lateinit var favouritesRepository: RoomFavouritesRepository
    private lateinit var remote: FakeRemote
    private lateinit var cache: FakeCache

    @Before
    fun setup() {
        dao = FakeFavouritesDao()
        favouritesRepository = RoomFavouritesRepository(dao, FakeProfileRepo())
        remote = FakeRemote()
        cache = FakeCache()
    }

    private fun createSync(
        ttlHours: Int = 24,
        backgroundEnabled: Boolean = true,
        scheduler: RecordingScheduler = RecordingScheduler()
    ) = FavouritesSyncRepositoryImpl(
        favouritesRepository = favouritesRepository,
        remote = remote,
        cache = cache,
        preferences = FakePreferences(ttlHours, backgroundEnabled),
        workScheduler = scheduler
    )

    @Test
    fun syncStaleFavourites_updatesOnlyStaleEntries() = runTest {
        val now = System.currentTimeMillis()
        val staleSyncedAt = now - (25L * 60L * 60L * 1000L)
        val freshSyncedAt = now - (1L * 60L * 60L * 1000L)

        dao.entries.addAll(
            listOf(
                FavouritePlanetEntity("p1", "1", now, null),
                FavouritePlanetEntity("p1", "2", now, staleSyncedAt),
                FavouritePlanetEntity("p1", "3", now, freshSyncedAt)
            )
        )
        remote.detailById = mapOf(
            "1" to planet("1", "Tatooine"),
            "2" to planet("2", "Alderaan")
        )

        val syncedCount = createSync().syncStaleFavourites(ttlHours = 24)

        assertEquals(2, syncedCount)
        assertEquals(listOf("1", "2"), remote.fetchedIds)
        assertEquals(2, cache.saved.size)
        assertNotNull(dao.entries.first { it.planetId == "1" }.lastSyncedAtMs)
        assertNotNull(dao.entries.first { it.planetId == "2" }.lastSyncedAtMs)
        assertEquals(freshSyncedAt, dao.entries.first { it.planetId == "3" }.lastSyncedAtMs)
        assertTrue(remote.fetchedIds.none { it == "3" })
    }

    @Test
    fun runBackgroundSync_skipsWhenBackgroundDisabled() = runTest {
        dao.entries.add(
            FavouritePlanetEntity("p1", "1", System.currentTimeMillis(), null)
        )
        remote.detailById = mapOf("1" to planet("1"))

        val syncedCount = createSync(backgroundEnabled = false).runBackgroundSync()

        assertEquals(0, syncedCount)
        assertTrue(remote.fetchedIds.isEmpty())
    }

    @Test
    fun scheduleImmediateSync_enqueuesOneTimeWork() {
        val scheduler = RecordingScheduler()
        createSync(scheduler = scheduler).scheduleImmediateSync()
        assertTrue(scheduler.oneTimeScheduled)
    }
}
