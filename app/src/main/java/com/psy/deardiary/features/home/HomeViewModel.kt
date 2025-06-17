package com.psy.deardiary.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.features.media.Article
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val feedItems: List<FeedItem> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFeedContent()
    }

    private fun loadFeedContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            journalRepository.journals.onEach { entries ->
                val feed = mutableListOf<FeedItem>()
                feed.add(FeedItem.WelcomeItem(getTimeOfDay(), "Odang"))
                feed.add(FeedItem.PromptItem("Apa satu hal yang membuatmu tersenyum hari ini?"))

                entries.forEach { entry ->
                    feed.add(FeedItem.JournalItem(entry))
                }

                if (entries.any { it.content.contains("cemas", true) || it.content.contains("stres", true) }) {
                    feed.add(FeedItem.ArticleSuggestionItem(Article("Memahami Overthinking", "Psikologi+", "https://placehold.co/600x400/D3E4F7/001D35?text=Artikel")))
                }

                _uiState.update { it.copy(isLoading = false, feedItems = feed) }
            }.launchIn(viewModelScope)
        }
    }

    fun saveQuickNote(content: String, mood: String = NEUTRAL_EMOJI) {
        viewModelScope.launch {
            if (content.isNotBlank()) {
                journalRepository.createJournal(
                    title = "", // Judul kosong menandakan Quick Entry
                    content = content,
                    mood = mood,
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