package com.example.swapiplanets.ui.notes

import com.example.swapiplanets.testutil.FakePlanetNotesRepository
import com.example.swapiplanets.testutil.MainDispatcherRule
import com.example.swapiplanets.ui.common.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun deleteNote_clearsContentToEmpty() = runTest {
        val notesRepo = FakePlanetNotesRepository()
        notesRepo.saveNote("1", "Tatooine", "Desert world")
        val vm = NotesViewModel(notesRepo)
        advanceUntilIdle()
        assertTrue(vm.state.value is UiState.Content)

        vm.deleteNote("1")
        advanceUntilIdle()
        assertTrue(vm.state.value is UiState.Empty)
    }
}
