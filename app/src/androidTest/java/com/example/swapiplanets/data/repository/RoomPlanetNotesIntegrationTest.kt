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
import com.example.swapiplanets.data.repository.RoomPlanetNotesRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomPlanetNotesIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var profileRepository: RoomUserProfileRepository
    private lateinit var repository: RoomPlanetNotesRepository

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
                produceFile = { context.preferencesDataStoreFile("test_notes.pb") }
            )
        )
        runBlocking { profileRepository.ensureDefaultProfile() }
        repository = RoomPlanetNotesRepository(database.planetNotesDao(), profileRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveNote_persistsAndEmitsInObserveAll() = runBlocking {
        repository.saveNote("5", "Dagobah", "Swamp training location")

        repository.observeAllNotes().test {
            val notes = awaitItem()
            assertEquals(1, notes.size)
            assertEquals("Swamp training location", notes.first().noteText)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteNote_removesFromDatabase() = runBlocking {
        repository.saveNote("5", "Dagobah", "Note")
        repository.deleteNote("5")

        repository.observePlanetIdsWithNotes().test {
            assertEquals(emptySet<String>(), awaitItem().toSet())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
