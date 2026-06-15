package com.example.swapiplanets.data.repository

import android.content.Context
import com.example.swapiplanets.data.local.AppDatabase
import com.example.swapiplanets.data.repository.RoomUserProfileRepository
import com.example.swapiplanets.data.repository.RoomFavouritesRepository
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swapiplanets.domain.repository.CreateProfileResult
import com.example.swapiplanets.domain.repository.DeleteProfileResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomUserProfileIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var profileRepository: RoomUserProfileRepository
    private lateinit var favouritesRepository: RoomFavouritesRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        profileRepository = RoomUserProfileRepository(
            database,
            database.userProfileDao(),
            PreferenceDataStoreFactory.create(
                produceFile = { context.preferencesDataStoreFile("test_user_profiles.pb") }
            )
        )
        favouritesRepository = RoomFavouritesRepository(database.favouritesDao(), profileRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun profiles_keepSeparateFavourites() = runTest {
        profileRepository.ensureDefaultProfile()
        favouritesRepository.toggle("1")
        assertEquals(setOf("1"), favouritesRepository.getAll())

        val defaultProfileId = profileRepository.getActiveProfileId()
        val secondResult = profileRepository.createProfile("Second")
        assertTrue(secondResult is CreateProfileResult.Created)
        favouritesRepository.toggle("2")
        assertEquals(setOf("2"), favouritesRepository.getAll())

        profileRepository.switchProfile(defaultProfileId)
        assertEquals(setOf("1"), favouritesRepository.getAll())
        assertNotEquals(setOf("2"), favouritesRepository.getAll())
    }

    @Test
    fun createProfile_limitsToMaxProfiles() = runTest {
        profileRepository.ensureDefaultProfile()
        repeat(RoomUserProfileRepository.MAX_PROFILES - 1) { index ->
            val result = profileRepository.createProfile("Profile $index")
            assertTrue(result is CreateProfileResult.Created)
        }
        assertEquals(CreateProfileResult.LimitReached, profileRepository.createProfile("Extra"))
    }

    @Test
    fun deleteProfile_removesProfileAndSwitchesActive() = runTest {
        profileRepository.ensureDefaultProfile()
        val firstId = profileRepository.getActiveProfileId()
        favouritesRepository.toggle("1")
        val second = profileRepository.createProfile("Second") as CreateProfileResult.Created

        assertEquals(DeleteProfileResult.Deleted, profileRepository.deleteProfile(firstId))
        assertEquals(second.profileId, profileRepository.getActiveProfileId())
        assertEquals(emptySet<String>(), favouritesRepository.getAll())
    }

    @Test
    fun deleteProfile_cannotDeleteLastProfile() = runTest {
        profileRepository.ensureDefaultProfile()
        val onlyId = profileRepository.getActiveProfileId()
        assertEquals(DeleteProfileResult.CannotDeleteLast, profileRepository.deleteProfile(onlyId))
    }
}
