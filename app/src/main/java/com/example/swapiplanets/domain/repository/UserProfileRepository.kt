package com.example.swapiplanets.domain.repository

import com.example.swapiplanets.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    companion object {
        const val MAX_PROFILES = 5
    }

    fun observeProfiles(): Flow<List<UserProfile>>
    fun observeActiveProfileId(): Flow<String>
    suspend fun ensureDefaultProfile()
    suspend fun getActiveProfileId(): String
    suspend fun createProfile(displayName: String): CreateProfileResult
    suspend fun deleteProfile(profileId: String): DeleteProfileResult
    suspend fun switchProfile(profileId: String)
}

sealed class CreateProfileResult {
    data class Created(val profileId: String) : CreateProfileResult()
    data object LimitReached : CreateProfileResult()
    data object InvalidName : CreateProfileResult()
}

sealed class DeleteProfileResult {
    data object Deleted : DeleteProfileResult()
    data object NotFound : DeleteProfileResult()
    data object CannotDeleteLast : DeleteProfileResult()
}
