package com.example.swapiplanets.ui.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swapiplanets.domain.model.RecentVisit
import com.example.swapiplanets.domain.repository.VisitHistoryRepository
import com.example.swapiplanets.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecentViewModel @Inject constructor(
    private val visitHistoryRepository: VisitHistoryRepository
) : ViewModel() {

    val state: StateFlow<UiState<List<RecentVisit>>> = visitHistoryRepository
        .observeRecent(limit = 30)
        .map { visits ->
            if (visits.isEmpty()) UiState.Empty else UiState.Content(visits)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)
}
