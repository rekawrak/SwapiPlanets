package com.example.swapiplanets.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.PlanetNote
import com.example.swapiplanets.domain.repository.PlanetNotesRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: PlanetNotesRepository
) : ViewModel() {

    val state: StateFlow<UiState<List<PlanetNote>>> = notesRepository.observeAllNotes()
        .map { notes ->
            if (notes.isEmpty()) UiState.Empty else UiState.Content(notes)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    fun deleteNote(planetId: String) {
        viewModelScope.launch {
            notesRepository.deleteNote(planetId)
        }
    }
}
