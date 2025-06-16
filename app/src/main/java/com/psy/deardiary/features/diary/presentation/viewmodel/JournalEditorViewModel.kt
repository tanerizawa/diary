// LOKASI: app/src/main/java/com/psy/deardiary/features/diary/JournalEditorViewModel.kt

package com.psy.deardiary.features.diary.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.navigation.Screen
import com.psy.deardiary.utils.AudioPlayer
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
    val voiceNotePath: String? = null,
    val isPlayingAudio: Boolean = false,
    val isDeleted: Boolean = false
)

@HiltViewModel
class JournalEditorViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalEditorUiState())
    val uiState = _uiState.asStateFlow()

    private var audioFile: File? = null

    init {
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

    fun deleteJournal() {
        viewModelScope.launch {
            // --- KODE YANG DIPERBAIKI ---
            val currentId = _uiState.value.entryId ?: return@launch
            // --- AKHIR KODE YANG DIPERBAIKI ---

            _uiState.update { it.copy(isLoading = true) }
            when (val result = journalRepository.deleteJournal(currentId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isDeleted = true) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onDeleteComplete() {
        _uiState.update { it.copy(isDeleted = false) }
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

    fun onPlaybackClicked() {
        val currentState = _uiState.value
        if (currentState.voiceNotePath == null) return

        if (currentState.isPlayingAudio) {
            audioPlayer.stop {
                _uiState.update { it.copy(isPlayingAudio = false) }
            }
        } else {
            audioPlayer.play(currentState.voiceNotePath) {
                _uiState.update { it.copy(isPlayingAudio = false) }
            }
            _uiState.update { it.copy(isPlayingAudio = true) }
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
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }
    }

    override fun onCleared() {
        audioPlayer.stop {}
        super.onCleared()
    }

    fun onSaveComplete() {
        _uiState.update { it.copy(isSaved = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}