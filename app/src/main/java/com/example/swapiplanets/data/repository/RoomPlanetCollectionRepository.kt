package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.local.PlanetCollectionDao
import com.example.swapiplanets.data.local.PlanetCollectionEntity
import com.example.swapiplanets.data.local.PlanetCollectionItemEntity
import com.example.swapiplanets.domain.model.PlanetCollection
import com.example.swapiplanets.domain.model.PlanetCollectionItem
import com.example.swapiplanets.domain.repository.PlanetCollectionRepository
import com.example.swapiplanets.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomPlanetCollectionRepository @Inject constructor(
    private val dao: PlanetCollectionDao,
    private val userProfileRepository: UserProfileRepository
) : PlanetCollectionRepository {

    override fun observeCollections(): Flow<List<PlanetCollection>> {
        return userProfileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            if (profileId.isBlank()) {
                flowOf(emptyList())
            } else {
                dao.observeByProfile(profileId).flatMapLatest { collections ->
                    if (collections.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        combine(
                            collections.map { collection ->
                                dao.observeItemCount(collection.collectionId).map { count ->
                                    PlanetCollection(
                                        collectionId = collection.collectionId,
                                        name = collection.name,
                                        createdAtMs = collection.createdAtMs,
                                        planetCount = count
                                    )
                                }
                            }
                        ) { it.toList() }
                    }
                }
            }
        }
    }

    override fun observeCollectionItems(collectionId: String): Flow<List<PlanetCollectionItem>> {
        return dao.observeItems(collectionId).map { items ->
            items.map {
                PlanetCollectionItem(
                    collectionId = it.collectionId,
                    planetId = it.planetId,
                    planetName = it.planetName,
                    addedAtMs = it.addedAtMs
                )
            }
        }
    }

    override fun observePlanetCollectionIds(planetId: String): Flow<Set<String>> {
        return userProfileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observeItemsForPlanet(profileId, planetId).map { items ->
                items.map { it.collectionId }.toSet()
            }
        }
    }

    override suspend fun createCollection(name: String): String {
        val profileId = userProfileRepository.getActiveProfileId()
        val trimmed = name.trim()
        require(trimmed.isNotEmpty())
        val id = UUID.randomUUID().toString()
        dao.insert(
            PlanetCollectionEntity(
                collectionId = id,
                profileId = profileId,
                name = trimmed,
                createdAtMs = System.currentTimeMillis()
            )
        )
        return id
    }

    override suspend fun deleteCollection(collectionId: String) {
        dao.delete(collectionId)
    }

    override suspend fun addPlanetToCollection(
        collectionId: String,
        planetId: String,
        planetName: String
    ) {
        val profileId = userProfileRepository.getActiveProfileId()
        dao.insertItem(
            PlanetCollectionItemEntity(
                collectionId = collectionId,
                profileId = profileId,
                planetId = planetId,
                planetName = planetName,
                addedAtMs = System.currentTimeMillis()
            )
        )
    }

    override suspend fun removePlanetFromCollection(collectionId: String, planetId: String) {
        dao.deleteItem(collectionId, planetId)
    }

    override suspend fun togglePlanetInCollection(
        collectionId: String,
        planetId: String,
        planetName: String
    ) {
        if (dao.isPlanetInCollection(collectionId, planetId)) {
            dao.deleteItem(collectionId, planetId)
        } else {
            addPlanetToCollection(collectionId, planetId, planetName)
        }
    }
}
