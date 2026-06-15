package com.example.swapiplanets.data.repository

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.swapiplanets.data.local.AppDatabase
import com.example.swapiplanets.data.repository.RoomUserProfileRepository
import com.example.swapiplanets.data.repository.RoomFavouritesRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomFavouritesRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var profileRepository: RoomUserProfileRepository
    private lateinit var repository: RoomFavouritesRepository

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
                produceFile = { context.preferencesDataStoreFile("test_favourites.pb") }
            )
        )
        runBlocking { profileRepository.ensureDefaultProfile() }
        repository = RoomFavouritesRepository(database.favouritesDao(), profileRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun repositoryAndRoom_togglePersistsAndReadsBack() = runBlocking {
        repository.toggle("7")

        assertTrue(repository.isFavourite("7"))
        assertEquals(setOf("7"), repository.getAll())

        repository.toggle("7")
        assertFalse(repository.isFavourite("7"))
        assertEquals(emptySet<String>(), repository.getAll())
    }

    @Test
    fun repositoryAndRoom_observeAllEmitsSequence() = runBlocking {
        repository.observeAll().test {
            assertEquals(emptySet<String>(), awaitItem())

            repository.toggle("1")
            assertEquals(setOf("1"), awaitItem())

            repository.toggle("2")
            assertEquals(setOf("1", "2"), awaitItem())
        }
    }
}
