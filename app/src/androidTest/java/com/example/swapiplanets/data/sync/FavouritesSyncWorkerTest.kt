package com.example.swapiplanets.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.swapiplanets.data.local.FavouritePlanetEntity
import com.example.swapiplanets.data.repository.RoomFavouritesRepository
import com.example.swapiplanets.data.repository.FavouritesSyncRepositoryImpl
import com.example.swapiplanets.domain.model.Planet
import com.example.swapiplanets.domain.model.UserProfile
import com.example.swapiplanets.domain.repository.CreateProfileResult
import com.example.swapiplanets.domain.repository.DeleteProfileResult
import com.example.swapiplanets.domain.repository.FavouritesSyncScheduler
import com.example.swapiplanets.domain.repository.PlanetCacheRepository
import com.example.swapiplanets.domain.repository.PlanetRemoteDataSource
import com.example.swapiplanets.domain.repository.PlanetUserPreferencesRepository
import com.example.swapiplanets.domain.repository.UserProfileRepository
// Removed external planet import to avoid cross-source-set dependency issues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouritesSyncWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @After
    fun tearDown() {
        WorkManagerTestInitHelper.closeWorkDatabase()
    }

    @Test
    fun doWork_runsBackgroundSync_andReturnsSuccess() = runTest {
        val trackingRemote = TrackingRemote()
        val syncRepository = FavouritesSyncRepositoryImpl(
            favouritesRepository = stubFavouritesRepository(
                listOf(FavouritePlanetEntity("p1", "1", System.currentTimeMillis(), null))
            ),
            remote = trackingRemote,
            cache = stubCache(),
            preferences = stubPreferences(backgroundEnabled = true),
            workScheduler = stubScheduler()
        )

        val worker = TestListenableWorkerBuilder<FavouritesSyncWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = FavouritesSyncWorker(appContext, workerParameters, syncRepository)
            })
            .build()

        val result = worker.startWork().get()

        assertTrue(result is ListenableWorker.Result.Success)
        assertEquals(listOf("1"), trackingRemote.fetchedIds)
    }

    private fun planet(id: String, name: String) = Planet(
        id = id,
        name = name,
        climate = "arid",
        terrain = "desert",
        population = "1",
        rotationPeriod = "1",
        orbitalPeriod = "1",
        diameter = "1"
    )

    private class TrackingRemote : PlanetRemoteDataSource {
        val fetchedIds = mutableListOf<String>()

        override suspend fun fetchPlanets(page: Int) = emptyList<Planet>()

        override suspend fun fetchPlanetDetail(id: String): Planet {
            fetchedIds.add(id)
            return planet(id, "Planet $id")
        }
    }

    private fun stubFavouritesRepository(
        entries: List<FavouritePlanetEntity>
    ): RoomFavouritesRepository {
        val stored = entries.toMutableList()
        val dao = object : com.example.swapiplanets.data.local.FavouritesDao {
            override suspend fun getAll(profileId: String) =
                stored.filter { it.profileId == profileId }

            override fun observeAll(profileId: String): Flow<List<FavouritePlanetEntity>> =
                flowOf(stored.filter { it.profileId == profileId })

            override suspend fun isFavourite(profileId: String, planetId: String) =
                stored.any { it.profileId == profileId && it.planetId == planetId }

            override suspend fun insert(entity: FavouritePlanetEntity) = 1L

            override suspend fun delete(entity: FavouritePlanetEntity) = Unit

            override suspend fun updateLastSynced(profileId: String, planetId: String, syncedAtMs: Long) {
                val index = stored.indexOfFirst { it.profileId == profileId && it.planetId == planetId }
                if (index >= 0) stored[index] = stored[index].copy(lastSyncedAtMs = syncedAtMs)
            }

            override suspend fun deleteByProfileId(profileId: String) = Unit
        }
        val profiles = object : UserProfileRepository {
            override fun observeProfiles(): Flow<List<UserProfile>> = flowOf(emptyList())
            override fun observeActiveProfileId(): Flow<String> = flowOf("p1")
            override suspend fun ensureDefaultProfile() = Unit
            override suspend fun getActiveProfileId() = "p1"
            override suspend fun createProfile(displayName: String) = CreateProfileResult.Created("p1")
            override suspend fun deleteProfile(profileId: String) = DeleteProfileResult.Deleted
            override suspend fun switchProfile(profileId: String) = Unit
        }
        return RoomFavouritesRepository(dao, profiles)
    }

    private fun stubCache(): PlanetCacheRepository = object : PlanetCacheRepository {
        override suspend fun save(planet: Planet) = Unit
        override suspend fun saveAll(planets: List<Planet>) = Unit
        override suspend fun get(id: String) = null
        override suspend fun getAll() = emptyList<Planet>()
        override suspend fun getByIds(ids: List<String>) = emptyList<Planet>()
        override fun isStale(cachedAtMs: Long, ttlHours: Int) = false
    }

    private fun stubPreferences(
        backgroundEnabled: Boolean = true
    ): PlanetUserPreferencesRepository = object : PlanetUserPreferencesRepository {
        override fun observeListOnlyFavourites(): Flow<Boolean> = flowOf(false)
        override suspend fun setListOnlyFavourites(enabled: Boolean) = Unit
        override fun observeSortNamesDescending(): Flow<Boolean> = flowOf(false)
        override suspend fun setSortNamesDescending(descending: Boolean) = Unit
        override fun observeCacheTtlHours(): Flow<Int> = flowOf(24)
        override suspend fun setCacheTtlHours(hours: Int) = Unit
        override fun observeBackgroundRefreshEnabled(): Flow<Boolean> = flowOf(backgroundEnabled)
        override suspend fun setBackgroundRefreshEnabled(enabled: Boolean) = Unit
    }

    private fun stubScheduler(): FavouritesSyncScheduler = object : FavouritesSyncScheduler {
        override fun schedulePeriodicSync() = Unit
        override fun scheduleOneTimeSync() = Unit
        override fun cancelPeriodicSync() = Unit
    }
}
