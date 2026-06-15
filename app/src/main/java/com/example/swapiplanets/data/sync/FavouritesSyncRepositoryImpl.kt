package com.example.swapiplanets.data.sync

import com.example.swapiplanets.domain.repository.FavouritesRepository
import com.example.swapiplanets.domain.repository.FavouritesSyncRepository
import com.example.swapiplanets.domain.repository.FavouritesSyncScheduler
import com.example.swapiplanets.domain.repository.PlanetCacheRepository
import com.example.swapiplanets.domain.repository.PlanetRemoteDataSource
import com.example.swapiplanets.domain.repository.PlanetUserPreferencesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouritesSyncRepositoryImpl @Inject constructor(
    private val favouritesRepository: FavouritesRepository,
    private val remote: PlanetRemoteDataSource,
    private val cache: PlanetCacheRepository,
    private val preferences: PlanetUserPreferencesRepository,
    private val workScheduler: FavouritesSyncScheduler
) : FavouritesSyncRepository {

    override suspend fun syncStaleFavourites(ttlHours: Int): Int {
        val entries = (favouritesRepository as? com.example.swapiplanets.data.repository.RoomFavouritesRepository)?.getAllEntries() ?: emptyList()
        var syncedCount = 0
        val now = System.currentTimeMillis()
        val ttlMs = ttlHours.coerceAtLeast(1) * 60L * 60L * 1000L

        for (entry in entries) {
            val lastSynced = entry.lastSyncedAtMs
            val isStale = lastSynced == null || (now - lastSynced) > ttlMs
            if (!isStale) continue

            runCatching {
                val planet = remote.fetchPlanetDetail(entry.planetId)
                cache.save(planet)
                (favouritesRepository as? com.example.swapiplanets.data.repository.RoomFavouritesRepository)?.markSynced(entry.planetId, now)
                syncedCount++
            }
        }
        return syncedCount
    }



    override suspend fun runBackgroundSync(): Int {
        val enabled = preferences.observeBackgroundRefreshEnabled().first()
        if (!enabled) return 0
        val ttl = preferences.observeCacheTtlHours().first()
        return syncStaleFavourites(ttl)
    }

    override fun scheduleBackgroundSync() {
        workScheduler.schedulePeriodicSync()
    }

    override fun cancelBackgroundSync() {
        workScheduler.cancelPeriodicSync()
    }

    override fun scheduleImmediateSync() {
        workScheduler.scheduleOneTimeSync()
    }
}