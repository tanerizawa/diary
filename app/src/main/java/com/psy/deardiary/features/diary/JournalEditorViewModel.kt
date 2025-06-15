// VERSI DIPERBARUI: Memastikan loadJournalEntry bersifat private.

package com.psy.deardiary.features.diary

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.navigation.Screen
import com.psy.deardiary.utils.AudioRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLDecoder
import javax.inject.Inject

data class JournalEditorUiState(
    val isLoading: Boolean = true,
    val journalTitle: String = "",
    val journalContent: String = "",
    val journalMood: String = "😊",
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val entryId: Int? = null,
    val isRecording: Boolean = false,
    val voiceNotePath: String? = null
)

@HiltViewModel
class JournalEditorViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val audioRecorder: AudioRecorder,
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalEditorUiState())
    val uiState = _uiState.asStateFlow()

    private var audioFile: File? = null

    init {
        // Logika untuk memuat data dipindahkan ke sini.
        // Ini akan berjalan sekali saat ViewModel dibuat.
        val entryId: Int? = savedStateHandle.get<String>(Screen.Editor.ENTRY_ID_ARG)?.toIntOrNull()
        val prompt: String? = savedStateHandle.get<String>(Screen.Editor.PROMPT_ARG)?.let {
            URLDecoder.decode(it, "UTF-8")
        }

        if (entryId != null) {
            loadJournalEntry(entryId)
        } else if (prompt != null) {
            _uiState.update { it.copy(journalContent = prompt, isLoading = false) }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // PERBAIKAN: Fungsi ini sekarang bersifat private, hanya untuk digunakan di dalam ViewModel ini.
    private fun loadJournalEntry(entryId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val entry = journalRepository.getJournalEntryById(entryId)
            if (entry != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        entryId = entry.id,
                        journalTitle = entry.title,
                        journalContent = entry.content,
                        journalMood = entry.mood,
                        voiceNotePath = entry.voiceNotePath
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Jurnal tidak ditemukan.") }
            }
        }
    }

    fun onStartRecording() {
        audioFile = File(context.cacheDir, "voice_journal_${System.currentTimeMillis()}.mp4")
        audioRecorder.start(audioFile!!)
        _uiState.update { it.copy(isRecording = true) }
    }

    fun onStopRecording() {
        audioRecorder.stop()
        _uiState.update { it.copy(isRecording = false, voiceNotePath = audioFile?.absolutePath) }
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
            val currentState = _uiState.value

            val result = if (currentState.entryId == null) {
                journalRepository.createJournal(
                    title = currentState.journalTitle,
                    content = currentState.journalContent,
                    mood = currentState.journalMood,
                    voiceNotePath = currentState.voiceNotePath
                )
            } else {
                journalRepository.updateJournal(
                    id = currentState.entryId,
                    title = currentState.journalTitle,
                    content = currentState.journalContent,
                    mood = currentState.journalMood,
                    voiceNotePath = currentState.voiceNotePath
                )
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
