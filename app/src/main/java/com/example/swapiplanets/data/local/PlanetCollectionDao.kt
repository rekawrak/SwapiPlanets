package com.example.swapiplanets.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanetCollectionDao {
    @Query("SELECT * FROM planet_collections WHERE profileId = :profileId ORDER BY createdAtMs ASC")
    fun observeByProfile(profileId: String): Flow<List<PlanetCollectionEntity>>

    @Query("SELECT * FROM planet_collections WHERE collectionId = :collectionId LIMIT 1")
    suspend fun getById(collectionId: String): PlanetCollectionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PlanetCollectionEntity)

    @Query("DELETE FROM planet_collections WHERE collectionId = :collectionId")
    suspend fun delete(collectionId: String)

    @Query("DELETE FROM planet_collections WHERE profileId = :profileId")
    suspend fun deleteByProfileId(profileId: String)

    @Query(
        "SELECT * FROM planet_collection_items WHERE collectionId = :collectionId ORDER BY addedAtMs ASC"
    )
    fun observeItems(collectionId: String): Flow<List<PlanetCollectionItemEntity>>

    @Query(
        "SELECT * FROM planet_collection_items WHERE profileId = :profileId AND planetId = :planetId"
    )
    fun observeItemsForPlanet(profileId: String, planetId: String): Flow<List<PlanetCollectionItemEntity>>

    @Query("SELECT COUNT(*) FROM planet_collection_items WHERE collectionId = :collectionId")
    fun observeItemCount(collectionId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(entity: PlanetCollectionItemEntity)

    @Query(
        "DELETE FROM planet_collection_items WHERE collectionId = :collectionId AND planetId = :planetId"
    )
    suspend fun deleteItem(collectionId: String, planetId: String)

    @Query(
        "SELECT EXISTS(" +
            "SELECT 1 FROM planet_collection_items " +
            "WHERE collectionId = :collectionId AND planetId = :planetId" +
            ")"
    )
    suspend fun isPlanetInCollection(collectionId: String, planetId: String): Boolean
}
