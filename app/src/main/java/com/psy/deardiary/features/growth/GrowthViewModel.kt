package com.psy.deardiary.features.growth // Ubah package menjadi growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class GrowthUiState( // Ubah nama dari HistoryUiState menjadi GrowthUiState
    val isLoading: Boolean = true,
    val totalJournals: Int = 0,
    val writingStreak: Int = 0,
    val mostFrequentMood: String = "-",
    val currentDisplayMonth: YearMonth = YearMonth.now(),
    val moodCalendarData: Map<Int, String> = emptyMap()
)

@HiltViewModel
class GrowthViewModel @Inject constructor( // Ubah nama kelas dari HistoryViewModel menjadi GrowthViewModel
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrowthUiState()) // Gunakan GrowthUiState
    val uiState = _uiState.asStateFlow()

    init {
        observeJournalData()
    }

    private fun observeJournalData() {
        journalRepository.journals
            .onEach { entries ->
                if (entries.isNotEmpty()) {
                    processJournalData(entries)
                } else {
                    _uiState.update { it.copy(isLoading = false, totalJournals = 0, writingStreak = 0, mostFrequentMood = "-", moodCalendarData = emptyMap()) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun processJournalData(entries: List<JournalEntry>) {
        val totalJournals = entries.size
        val mostFrequentMood = calculateMostFrequentMood(entries)
        val writingStreak = calculateWritingStreak(entries)

        val currentMonth = _uiState.value.currentDisplayMonth
        val moodCalendarData = generateMoodCalendarData(entries, currentMonth)

        _uiState.update {
            it.copy(
                isLoading = false,
                totalJournals = totalJournals,
                mostFrequentMood = mostFrequentMood,
                writingStreak = writingStreak,
                moodCalendarData = moodCalendarData
            )
        }
    }

    private fun calculateMostFrequentMood(entries: List<JournalEntry>): String {
        return entries.groupingBy { it.mood }.eachCount().maxByOrNull { it.value }?.key ?: "-"
    }

    private fun calculateWritingStreak(entries: List<JournalEntry>): Int {
        if (entries.isEmpty()) return 0

        val entryDates = entries
            .map { Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() }
            .toSortedSet(compareByDescending { it })

        var streak = 0
        var currentDate = LocalDate.now()

        // Jika hari ini belum menulis dan kemarin menulis, mulai hitung dari kemarin
        if (!entryDates.contains(currentDate) && entryDates.contains(currentDate.minusDays(1))) {
            currentDate = currentDate.minusDays(1)
        }

        while (entryDates.contains(currentDate)) {
            streak++
            currentDate = currentDate.minusDays(1)
        }

        return streak
    }

    private fun generateMoodCalendarData(entries: List<JournalEntry>, yearMonth: YearMonth): Map<Int, String> {
        return entries
            .filter {
                val entryDate = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                entryDate.year == yearMonth.year && entryDate.month == yearMonth.month
            }
            .associate {
                val entryDate = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                entryDate.dayOfMonth to it.mood
            }
    }

    fun changeDisplayMonth(offset: Long) {
        val newMonth = _uiState.value.currentDisplayMonth.plusMonths(offset)
        _uiState.update { it.copy(currentDisplayMonth = newMonth) }
    }
}