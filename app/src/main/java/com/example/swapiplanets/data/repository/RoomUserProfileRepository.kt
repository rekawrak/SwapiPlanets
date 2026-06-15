package com.example.swapiplanets.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.withTransaction
import com.example.swapiplanets.data.local.AppDatabase
import com.example.swapiplanets.data.local.UserProfileDao
import com.example.swapiplanets.data.local.UserProfileEntity
import com.example.swapiplanets.domain.model.UserProfile
import com.example.swapiplanets.domain.repository.CreateProfileResult
import com.example.swapiplanets.domain.repository.DeleteProfileResult
import com.example.swapiplanets.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val KEY_ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")

@Singleton
class RoomUserProfileRepository @Inject constructor(
    private val database: AppDatabase,
    private val profileDao: UserProfileDao,
    private val dataStore: DataStore<Preferences>
) : UserProfileRepository {

    override fun observeProfiles(): Flow<List<UserProfile>> {
        return profileDao.observeAll().map { entities ->
            entities.map { UserProfile(it.profileId, it.displayName, it.createdAtMs) }
        }
    }

    override fun observeActiveProfileId(): Flow<String> {
        return dataStore.data
            .map { prefs -> prefs[KEY_ACTIVE_PROFILE_ID].orEmpty() }
            .distinctUntilChanged()
    }

    override suspend fun ensureDefaultProfile() {
        val profiles = profileDao.getAll()
        if (profiles.isEmpty()) {
            val defaultId = UUID.randomUUID().toString()
            profileDao.insert(
                UserProfileEntity(
                    profileId = defaultId,
                    displayName = "Default",
                    createdAtMs = System.currentTimeMillis()
                )
            )
            dataStore.edit { it[KEY_ACTIVE_PROFILE_ID] = defaultId }
            return
        }
        val activeId = dataStore.data.first()[KEY_ACTIVE_PROFILE_ID]
        if (activeId.isNullOrBlank() || profileDao.getById(activeId) == null) {
            dataStore.edit { it[KEY_ACTIVE_PROFILE_ID] = profiles.first().profileId }
        }
    }

    override suspend fun getActiveProfileId(): String {
        ensureDefaultProfile()
        return observeActiveProfileId().first().ifBlank {
            profileDao.getAll().first().profileId
        }
    }

    override suspend fun createProfile(displayName: String): CreateProfileResult {
        val trimmed = displayName.trim()
        if (trimmed.isEmpty()) return CreateProfileResult.InvalidName
        if (profileDao.count() >= UserProfileRepository.MAX_PROFILES) return CreateProfileResult.LimitReached

        val profileId = UUID.randomUUID().toString()
        profileDao.insert(
            UserProfileEntity(
                profileId = profileId,
                displayName = trimmed,
                createdAtMs = System.currentTimeMillis()
            )
        )
        switchProfile(profileId)
        return CreateProfileResult.Created(profileId)
    }

    override suspend fun deleteProfile(profileId: String): DeleteProfileResult {
        if (profileDao.getById(profileId) == null) return DeleteProfileResult.NotFound
        if (profileDao.count() <= 1) return DeleteProfileResult.CannotDeleteLast

        database.withTransaction {
            database.favouritesDao().deleteByProfileId(profileId)
            database.planetNotesDao().deleteByProfileId(profileId)
            database.visitHistoryDao().deleteByProfileId(profileId)
            database.planetCollectionDao().deleteByProfileId(profileId)
            database.planetUserStateDao().deleteByProfileId(profileId)
            profileDao.delete(profileId)
        }

        val activeId = dataStore.data.first()[KEY_ACTIVE_PROFILE_ID]
        if (activeId == profileId) {
            val remaining = profileDao.getAll()
            if (remaining.isNotEmpty()) {
                dataStore.edit { it[KEY_ACTIVE_PROFILE_ID] = remaining.first().profileId }
            }
        }
        return DeleteProfileResult.Deleted
    }

    override suspend fun switchProfile(profileId: String) {
        checkNotNull(profileDao.getById(profileId)) { "Profile not found" }
        dataStore.edit { it[KEY_ACTIVE_PROFILE_ID] = profileId }
    }

    companion object {
        const val MAX_PROFILES = UserProfileRepository.MAX_PROFILES
    }
}
