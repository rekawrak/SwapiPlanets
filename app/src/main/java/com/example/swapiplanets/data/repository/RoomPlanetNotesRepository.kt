package com.example.swapiplanets.data.repository

import com.example.swapiplanets.data.local.PlanetNoteEntity
import com.example.swapiplanets.data.local.PlanetNotesDao
import com.example.swapiplanets.data.mapper.toDomain
import com.example.swapiplanets.domain.model.PlanetNote
import com.example.swapiplanets.domain.repository.PlanetNotesRepository
import com.example.swapiplanets.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomPlanetNotesRepository @Inject constructor(
    private val dao: PlanetNotesDao,
    private val userProfileRepository: UserProfileRepository
) : PlanetNotesRepository {

    override fun observeNote(planetId: String): Flow<String?> {
        return userProfileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observeByPlanetId(profileId, planetId).map { entity -> entity?.noteText }
        }
    }

    override fun observeAllNotes(): Flow<List<PlanetNote>> {
        return userProfileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observeAll(profileId).map { entities -> entities.map { it.toDomain() } }
        }
    }

    override fun observePlanetIdsWithNotes(): Flow<Set<String>> {
        return userProfileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observePlanetIdsWithNotes(profileId).map { it.toSet() }
        }
    }

    override suspend fun saveNote(planetId: String, planetName: String, noteText: String) {
        val profileId = userProfileRepository.getActiveProfileId()
        val trimmed = noteText.trim()
        if (trimmed.isEmpty()) {
            dao.delete(profileId, planetId)
            return
        }
        dao.upsert(
            PlanetNoteEntity(
                profileId = profileId,
                planetId = planetId,
                planetName = planetName,
                noteText = trimmed,
                updatedAtMs = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteNote(planetId: String) {
        val profileId = userProfileRepository.getActiveProfileId()
        dao.delete(profileId, planetId)
    }
}
