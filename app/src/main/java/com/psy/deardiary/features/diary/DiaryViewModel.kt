// LOKASI: app/src/main/java/com/psy/deardiary/features/diary/DiaryViewModel.kt

package com.psy.deardiary.features.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// PERBAIKAN: Kita akan menggunakan model 'JournalEntry' secara langsung di UI,
// jadi kita tidak memerlukan 'DiaryEntryItem' lagi.
data class DiaryUiState(
    val isLoading: Boolean = true,
    val entries: List<JournalEntry> = emptyList(), // Menggunakan JournalEntry secara langsung
    val error: String? = null
)

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeLocalJournals()
        refreshJournals()
        syncJournals()
    }

    private fun observeLocalJournals() {
        journalRepository.journals
            .onEach { journalEntries ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        // PERBAIKAN: Tidak perlu lagi memetakan ke DiaryEntryItem
                        entries = journalEntries
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refreshJournals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = journalRepository.refreshJournals()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    private fun syncJournals() {
        viewModelScope.launch {
            journalRepository.syncPendingJournals()
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(error = null) }
    }
}