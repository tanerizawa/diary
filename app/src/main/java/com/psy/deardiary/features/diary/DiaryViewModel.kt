// File: app/src/main/java/com/psy/deardiary/features/diary/DiaryViewModel.kt
// VERSI DIPERBARUI: Menyesuaikan pemanggilan createJournal dengan parameter baru.

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// Model data untuk UI, tidak perlu diubah.
data class DiaryEntryItem(
    val id: Int,
    val title: String,
    val contentPreview: String,
    val mood: String,
    val date: String
)

data class DiaryUiState(
    val isLoading: Boolean = true, // Default ke true saat pertama kali dimuat
    val entries: List<DiaryEntryItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // 1. Mulai mengamati data dari database lokal.
        observeLocalJournals()
        // 2. Segarkan data dari server saat pertama kali ViewModel dibuat.
        refreshJournals()
        // 3. Coba sinkronkan data yang tertunda.
        syncJournals()
    }

    private fun observeLocalJournals() {
        journalRepository.journals
            .onEach { journalEntries ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false, // Berhenti loading setelah data pertama diterima
                        entries = journalEntries.map { it.toDiaryEntryItem() }
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
                    // Tidak perlu melakukan apa-apa di sini, karena `observeLocalJournals`
                    // akan otomatis menangkap data baru. Cukup matikan status loading.
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun createJournal(title: String, content: String, mood: String) {
        viewModelScope.launch {
            // PERBAIKAN: Menambahkan argumen 'voiceNotePath = null' yang hilang.
            journalRepository.createJournal(
                title = title,
                content = content,
                mood = mood,
                voiceNotePath = null
            )
            // Setelah membuat jurnal lokal, coba sinkronkan langsung.
            syncJournals()
        }
    }

    private fun syncJournals() {
        viewModelScope.launch {
            // Proses sinkronisasi bisa berjalan di latar belakang tanpa
            // perlu memblokir atau menampilkan loading di UI secara eksplisit.
            journalRepository.syncPendingJournals()
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * Fungsi mapper untuk mengubah model database (JournalEntry) menjadi model UI (DiaryEntryItem).
 */
private fun JournalEntry.toDiaryEntryItem(): DiaryEntryItem {
    val simpleDateFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
    val formattedDate = simpleDateFormat.format(Date(this.timestamp))
    val preview = if (this.content.length > 100) {
        this.content.substring(0, 100) + "..."
    } else {
        this.content
    }
    return DiaryEntryItem(
        id = this.id, // Menggunakan ID lokal untuk key di UI
        title = this.title.ifEmpty { "Tanpa Judul" },
        contentPreview = preview,
        mood = this.mood,
        date = formattedDate
    )
}
