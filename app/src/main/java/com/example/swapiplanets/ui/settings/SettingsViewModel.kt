package com.example.swapiplanets.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.UserProfile
import com.example.swapiplanets.domain.repository.CreateProfileResult
import com.example.swapiplanets.domain.repository.DeleteProfileResult
import com.example.swapiplanets.domain.repository.FavouritesSyncRepository
import com.example.swapiplanets.domain.repository.PlanetUserPreferencesRepository
import com.example.swapiplanets.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PlanetUserPreferencesRepository,
    private val favouritesSyncRepository: FavouritesSyncRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    val maxProfiles: Int = UserProfileRepository.MAX_PROFILES

    val cacheTtlHours: StateFlow<Int> = preferences.observeCacheTtlHours()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 24)

    val backgroundRefreshEnabled: StateFlow<Boolean> = preferences.observeBackgroundRefreshEnabled()
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val profiles: StateFlow<List<UserProfile>> = userProfileRepository.observeProfiles()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activeProfileId: StateFlow<String> = userProfileRepository.observeActiveProfileId()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    private val _profileFeedback = MutableStateFlow<String?>(null)
    val profileFeedback: StateFlow<String?> = _profileFeedback.asStateFlow()

    fun setCacheTtlHours(hours: Int) {
        viewModelScope.launch {
            preferences.setCacheTtlHours(hours)
        }
    }

    fun setBackgroundRefreshEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBackgroundRefreshEnabled(enabled)
            if (enabled) {
                favouritesSyncRepository.scheduleBackgroundSync()
            } else {
                favouritesSyncRepository.cancelBackgroundSync()
            }
        }
    }

    fun createProfile(name: String) {
        viewModelScope.launch {
            when (userProfileRepository.createProfile(name)) {
                is CreateProfileResult.Created -> _profileFeedback.value = null
                CreateProfileResult.InvalidName ->
                    _profileFeedback.value = "Profile name cannot be empty."
                CreateProfileResult.LimitReached ->
                    _profileFeedback.value = "Maximum $maxProfiles profiles allowed."
            }
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            when (userProfileRepository.deleteProfile(profileId)) {
                DeleteProfileResult.Deleted -> _profileFeedback.value = null
                DeleteProfileResult.CannotDeleteLast ->
                    _profileFeedback.value = "Cannot delete the only remaining profile."
                DeleteProfileResult.NotFound ->
                    _profileFeedback.value = "Profile not found."
            }
        }
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            userProfileRepository.switchProfile(profileId)
        }
    }
}
