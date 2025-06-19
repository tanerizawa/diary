package com.psy.deardiary.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.data.repository.UserRepository
import com.psy.deardiary.data.repository.FeedRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.features.home.FeedItem

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
    val userName: String = "",
    val lastMood: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val userRepository: UserRepository,
    private val feedRepository: FeedRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _feedItems = MutableStateFlow<List<FeedItem>>(emptyList())
    val feedItems = _feedItems.asStateFlow()

    init {
        viewModelScope.launch {
            val name = when (val result = userRepository.getProfile()) {
                is Result.Success -> result.data.name.orEmpty()
                else -> ""
            }
            val mood = journalRepository.getLatestMood()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    timeOfDay = getTimeOfDay(),
                    userName = name,
                    lastMood = mood
                )
            }

            when (val feedResult = feedRepository.getFeed()) {
                is Result.Success -> _feedItems.value = feedResult.data
                else -> Unit
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
