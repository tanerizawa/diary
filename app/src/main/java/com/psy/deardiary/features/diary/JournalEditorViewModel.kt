package com.psy.deardiary.features.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JournalEditorUiState(
    val isLoading: Boolean = false,
    val journalTitle: String = "",
    val journalContent: String = "",
    val journalMood: String = "😊",
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val entryId: Int? = null // ID lokal dari entri yang sedang diedit
)

@HiltViewModel
class JournalEditorViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalEditorUiState())
    val uiState = _uiState.asStateFlow()

    fun loadJournalEntry(entryId: Int?) {
        if (entryId == null) {
            // Ini adalah entri baru, reset state
            _uiState.update { JournalEditorUiState(isLoading = false) } // Pastikan isLoading false untuk entri baru
            return
        }

        // Jika ID sama dan data sudah ada, tidak perlu muat ulang
        if (uiState.value.entryId == entryId && uiState.value.journalTitle.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val entry = journalRepository.getJournalEntryById(entryId) // Memanggil fungsi baru di repository
            if (entry != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        entryId = entry.id,
                        journalTitle = entry.title,
                        journalContent = entry.content,
                        journalMood = entry.mood
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Jurnal tidak ditemukan."
                    )
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(journalTitle = title) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(journalContent = content) }
    }

    fun updateMood(mood: String) {
        _uiState.update { it.copy(journalMood = mood) }
    }

    fun saveJournal() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val currentTitle = _uiState.value.journalTitle
            val currentContent = _uiState.value.journalContent
            val currentMood = _uiState.value.journalMood
            val currentEntryId = _uiState.value.entryId

            val result = if (currentEntryId == null) {
                // Buat jurnal baru
                journalRepository.createJournal(currentTitle, currentContent, currentMood)
            } else {
                // Perbarui jurnal yang sudah ada
                journalRepository.updateJournal(currentEntryId, currentTitle, currentContent, currentMood)
            }

            when (result) {
                is com.psy.deardiary.data.repository.Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                is com.psy.deardiary.data.repository.Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onSaveComplete() {
        _uiState.update { it.copy(isSaved = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}