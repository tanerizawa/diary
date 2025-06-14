package com.psy.deardiary.features.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.psy.deardiary.ui.components.PrimaryButton // Import yang hilang
import com.psy.deardiary.ui.theme.DearDiaryTheme // Import yang hilang

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    entryId: Int? = null,
    onBackClick: () -> Unit,
    viewModel: JournalEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(entryId) {
        if (entryId != uiState.entryId || (entryId == null && uiState.entryId != null)) {
            viewModel.loadJournalEntry(entryId)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBackClick()
            viewModel.onSaveComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entryId == null) "Jurnal Baru" else "Edit Jurnal") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveJournal() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Save, "Simpan Jurnal")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && entryId != null && uiState.journalTitle.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.journalTitle,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Judul (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.journalContent,
                    onValueChange = { viewModel.updateContent(it) },
                    label = { Text("Apa yang kamu rasakan hari ini?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Bagaimana mood-mu saat ini?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val moods = listOf("😊", "😟", "😠", "😢", "😐")
                    moods.forEach { mood ->
                        MoodSelector(
                            mood = mood,
                            isSelected = mood == uiState.journalMood,
                            // Perbaikan: Bungkus onSelect dalam lambda tanpa argumen
                            onSelect = { selectedMood -> viewModel.updateMood(selectedMood) } // Pass the selectedMood from lambda
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton( // Baris 142
                    text = "Jurnal Suara",
                    onClick = { /* TODO: Implementasi logika jurnal suara */ },
                    enabled = !uiState.isLoading
                )
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            println("Error: $message")
            viewModel.clearErrorMessage()
        }
    }
}

@Composable
private fun MoodSelector(
    mood: String,
    isSelected: Boolean,
    onSelect: (String) -> Unit // Menerima String
) {
    val color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    TextButton(
        // Perbaikan: Panggil onSelect dengan argumen 'mood'
        onClick = { onSelect(mood) }, // Baris 167 (sebelumnya 167)
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = color
        )
    ) {
        Text(text = mood, style = MaterialTheme.typography.headlineLarge) // Baris 173 (sebelumnya 173)
    }
}

@Preview(showBackground = true)
@Composable
private fun JournalEditorScreenPreview() { // Baris 180 (sebelumnya 180)
    DearDiaryTheme { // Baris 181 (sebelumnya 181)
        JournalEditorScreen(onBackClick = {})
    }
}