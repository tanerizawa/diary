// File: app/src/main/java/com/psy/deardiary/features/growth/GrowthViewModel.kt
// VERSI DIPERBARUI: Menambahkan logika untuk data tren mood.

package com.psy.deardiary.features.growth

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
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// Data class baru untuk titik data pada grafik
data class MoodDataPoint(val label: String, val averageMood: Float)

data class GrowthUiState(
    val isLoading: Boolean = true,
    val totalJournals: Int = 0,
    val writingStreak: Int = 0,
    val mostFrequentMood: String = "-",
    val currentDisplayMonth: YearMonth = YearMonth.now(),
    val moodCalendarData: Map<Int, String> = emptyMap(),
    // State baru untuk data grafik tren mood
    val moodTrendData: List<MoodDataPoint> = emptyList()
)

@HiltViewModel
class GrowthViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrowthUiState())
    val uiState = _uiState.asStateFlow()

    private var cachedEntries: List<JournalEntry> = emptyList()

    init {
        observeJournalData()
    }

    private fun observeJournalData() {
        journalRepository.journals
            .onEach { entries ->
                cachedEntries = entries
                processJournalData(entries)
            }
            .launchIn(viewModelScope)
    }

    private fun processJournalData(entries: List<JournalEntry>) {
        if (entries.isEmpty()) {
            _uiState.update {
                it.copy(isLoading = false, totalJournals = 0, writingStreak = 0, mostFrequentMood = "-", moodCalendarData = emptyMap(), moodTrendData = emptyList())
            }
            return
        }

        val totalJournals = entries.size
        val mostFrequentMood = calculateMostFrequentMood(entries)
        val writingStreak = calculateWritingStreak(entries)
        val currentMonth = _uiState.value.currentDisplayMonth
        val moodCalendarData = generateMoodCalendarData(entries, currentMonth)
        val moodTrendData = calculateMoodTrend(entries)

        _uiState.update {
            it.copy(
                isLoading = false,
                totalJournals = totalJournals,
                mostFrequentMood = mostFrequentMood,
                writingStreak = writingStreak,
                moodCalendarData = moodCalendarData,
                moodTrendData = moodTrendData
            )
        }
    }

    private fun calculateMoodTrend(entries: List<JournalEntry>): List<MoodDataPoint> {
        val moodScores = mapOf("😊" to 5f, "😐" to 3f, "😟" to 2f, "😠" to 1f, "😢" to 1f)
        val today = LocalDate.now()
        val last7DaysEntries = entries.filter {
            val entryDate = Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            !entryDate.isBefore(today.minusDays(6)) && !entryDate.isAfter(today)
        }

        if (last7DaysEntries.isEmpty()) return emptyList()

        val groupedByDay = last7DaysEntries.groupBy {
            Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        val dailyAverages = (0..6).map { i ->
            val date = today.minusDays(i.toLong())
            val entriesForDay = groupedByDay[date]
            val averageScore = if (entriesForDay != null && entriesForDay.isNotEmpty()) {
                entriesForDay.mapNotNull { moodScores[it.mood] }.average().toFloat()
            } else {
                0f
            }
            date to averageScore
        }.reversed()

        val formatter = DateTimeFormatter.ofPattern("dd/MM")
        return dailyAverages.map { (date, avg) ->
            MoodDataPoint(label = date.format(formatter), averageMood = avg)
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
        val newMoodCalendarData = generateMoodCalendarData(cachedEntries, newMonth)
        _uiState.update {
            it.copy(
                currentDisplayMonth = newMonth,
                moodCalendarData = newMoodCalendarData
            )
        }
    }
}
