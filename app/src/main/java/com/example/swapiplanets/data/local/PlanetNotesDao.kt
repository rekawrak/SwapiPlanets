package com.example.swapiplanets.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanetNotesDao {
    @Query(
        "SELECT * FROM planet_notes WHERE profileId = :profileId AND planetId = :planetId LIMIT 1"
    )
    fun observeByPlanetId(profileId: String, planetId: String): Flow<PlanetNoteEntity?>

    @Query("SELECT * FROM planet_notes WHERE profileId = :profileId ORDER BY updatedAtMs DESC")
    fun observeAll(profileId: String): Flow<List<PlanetNoteEntity>>

    @Query("SELECT planetId FROM planet_notes WHERE profileId = :profileId")
    fun observePlanetIdsWithNotes(profileId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PlanetNoteEntity)

    @Query("DELETE FROM planet_notes WHERE profileId = :profileId AND planetId = :planetId")
    suspend fun delete(profileId: String, planetId: String)

    @Query("DELETE FROM planet_notes WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: String)
}
