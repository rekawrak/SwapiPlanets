package com.example.swapiplanets.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanetUserStateDao {
    @Query("SELECT * FROM planet_user_states WHERE profileId = :profileId")
    fun observeAll(profileId: String): Flow<List<PlanetUserStateEntity>>

    @Query("SELECT * FROM planet_user_states WHERE profileId = :profileId AND planetId = :planetId LIMIT 1")
    suspend fun get(profileId: String, planetId: String): PlanetUserStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlanetUserStateEntity)

    @Query(
        "SELECT planetId FROM planet_user_states " +
            "WHERE profileId = :profileId AND isPinned = 1 ORDER BY pinnedOrder ASC"
    )
    fun observePinnedPlanetIds(profileId: String): Flow<List<String>>

    @Query(
        "SELECT COUNT(*) FROM planet_user_states WHERE profileId = :profileId AND isPinned = 1"
    )
    suspend fun pinnedCount(profileId: String): Int

    @Query(
        "SELECT planetId FROM planet_user_states " +
            "WHERE profileId = :profileId AND isPinned = 1 ORDER BY pinnedOrder ASC"
    )
    suspend fun getPinnedPlanetIds(profileId: String): List<String>

    @Query("DELETE FROM planet_user_states WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: String)
}
