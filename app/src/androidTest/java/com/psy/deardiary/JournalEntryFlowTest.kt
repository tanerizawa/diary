package com.psy.deardiary

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.psy.deardiary.features.diary.JournalEditorScreen
import com.psy.deardiary.features.diary.JournalEditorViewModel
import com.psy.deardiary.features.main.MainViewModel
import com.psy.deardiary.fakes.TestRepositories
import com.psy.deardiary.utils.AudioPlayer
import com.psy.deardiary.utils.AudioRecorder
import org.junit.Rule
import org.junit.Test

class JournalEntryFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createJournalAndShowInHistory() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repo = TestRepositories.journalRepository()
        val viewModel = JournalEditorViewModel(
            repo,
            AudioRecorder(context),
            AudioPlayer(),
            context,
            SavedStateHandle()
        )
        val mainViewModel = MainViewModel()

        composeRule.setContent {
            var showHistory by remember { mutableStateOf(false) }
            if (showHistory) {
                val entries by repo.journals.collectAsState(initial = emptyList())
                LazyColumn {
                    items(entries) { entry ->
                        androidx.compose.material3.Text(entry.content)
                    }
                }
            } else {
                JournalEditorScreen(
                    onBackClick = { showHistory = true },
                    viewModel = viewModel,
                    mainViewModel = mainViewModel
                )
                val uiState by viewModel.uiState.collectAsState()
                if (uiState.isSaved) {
                    showHistory = true
                    viewModel.onSaveComplete()
                }
            }
        }

        composeRule.onNodeWithText("Judul (Opsional)").performTextInput("Test")
        composeRule.onNodeWithText("Apa yang kamu rasakan hari ini?").performTextInput("Hello world")
        composeRule.onNodeWithContentDescription("Simpan Jurnal").performClick()

        composeRule.onNodeWithText("Hello world").assertIsDisplayed()
    }
}
