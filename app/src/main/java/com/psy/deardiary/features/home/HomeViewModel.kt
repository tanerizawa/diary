package com.psy.deardiary.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.repository.JournalRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val timeOfDay: String = "",
    val userName: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    timeOfDay = getTimeOfDay(),
                    userName = "Odang"
                )
            }
        }
    }

    fun saveQuickNote(content: String, mood: String = NEUTRAL_EMOJI) {
        viewModelScope.launch {
            if (content.isNotBlank()) {
                val sanitizedMood = if (mood == NEUTRAL_EMOJI) "" else mood
                journalRepository.createJournal(
                    title = "", // Judul kosong menandakan Quick Entry
                    content = content,
                    mood = sanitizedMood,
                    voiceNotePath = null
                )
            }
        }
    }

    private fun getTimeOfDay(): String {
        return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 4..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            in 15..18 -> "Selamat Sore"
            else -> "Selamat Malam"
        }
    }
}
