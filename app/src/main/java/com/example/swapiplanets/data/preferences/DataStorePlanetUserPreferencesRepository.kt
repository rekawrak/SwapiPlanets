package com.example.swapiplanets.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.swapiplanets.domain.repository.PlanetUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val KEY_LIST_ONLY_FAVOURITES = booleanPreferencesKey("list_only_favourites")
private val KEY_SORT_NAMES_DESC = booleanPreferencesKey("sort_names_desc")
private val KEY_CACHE_TTL_HOURS = intPreferencesKey("cache_ttl_hours")
private val KEY_BACKGROUND_REFRESH = booleanPreferencesKey("background_refresh_enabled")

@Singleton
class DataStorePlanetUserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PlanetUserPreferencesRepository {

    override fun observeListOnlyFavourites(): Flow<Boolean> {
        return dataStore.data
            .map { prefs -> prefs[KEY_LIST_ONLY_FAVOURITES] ?: false }
            .distinctUntilChanged()
    }

    override suspend fun setListOnlyFavourites(enabled: Boolean) {
        dataStore.edit { it[KEY_LIST_ONLY_FAVOURITES] = enabled }
    }

    override fun observeSortNamesDescending(): Flow<Boolean> {
        return dataStore.data
            .map { prefs -> prefs[KEY_SORT_NAMES_DESC] ?: false }
            .distinctUntilChanged()
    }

    override suspend fun setSortNamesDescending(descending: Boolean) {
        dataStore.edit { it[KEY_SORT_NAMES_DESC] = descending }
    }

    override fun observeCacheTtlHours(): Flow<Int> {
        return dataStore.data
            .map { prefs -> prefs[KEY_CACHE_TTL_HOURS] ?: DEFAULT_CACHE_TTL_HOURS }
            .distinctUntilChanged()
    }

    override suspend fun setCacheTtlHours(hours: Int) {
        dataStore.edit { it[KEY_CACHE_TTL_HOURS] = hours.coerceIn(1, 168) }
    }

    override fun observeBackgroundRefreshEnabled(): Flow<Boolean> {
        return dataStore.data
            .map { prefs -> prefs[KEY_BACKGROUND_REFRESH] ?: true }
            .distinctUntilChanged()
    }

    override suspend fun setBackgroundRefreshEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_BACKGROUND_REFRESH] = enabled }
    }

    companion object {
        const val DEFAULT_CACHE_TTL_HOURS = 24
    }
}
