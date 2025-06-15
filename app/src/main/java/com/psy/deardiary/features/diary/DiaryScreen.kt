package com.psy.deardiary.features.diary

import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psy.deardiary.ui.components.AnimatedFadeIn
import com.psy.deardiary.ui.components.JournalEntryCard
import com.psy.deardiary.ui.components.NetworkErrorSnackbar
import com.psy.deardiary.ui.theme.DearDiaryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    state: DiaryUiState,
    onNavigateToEditor: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCrisisSupport: () -> Unit,
    onEntryClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(message = errorMessage)
            onClearError()
        }
    }

    Scaffold(
        snackbarHost = {
            NetworkErrorSnackbar(
                snackbarHostState = snackbarHostState,
                onRetry = onRetry
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Dear Diary", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToCrisisSupport) {
                        Icon(
                            imageVector = Icons.Outlined.CrisisAlert,
                            contentDescription = "Dukungan Krisis",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Pengaturan",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToEditor,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Filled.Add, "Tambah Jurnal Baru")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Tampilkan loading indicator hanya jika data sedang dimuat DAN belum ada entri sama sekali.
                state.isLoading && state.entries.isEmpty() -> {
                    CircularProgressIndicator()
                }
                // PERBAIKAN: Kondisi disederhanakan. Ini hanya akan tercapai jika isLoading false dan entries kosong.
                state.entries.isEmpty() -> {
                    EmptyState()
                }
                // Jika ada entri, tampilkan list, terlepas dari status loading (untuk refresh di latar belakang).
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = state.entries, key = { it.id }) { entry ->
                            AnimatedFadeIn {
                                JournalEntryCard(
                                    title = entry.title,
                                    contentPreview = entry.contentPreview,
                                    mood = entry.mood,
                                    date = entry.date,
                                    onClick = { onEntryClick(entry.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Text(
            text = "📖",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ruang Amanmu Menanti",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Mulai tulis jurnal pertamamu untuk memulai perjalanan refleksi diri.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Preview(showBackground = true, name = "Layar dengan Entri")
@Composable
private fun DiaryScreenPreview_WithEntries() {
    val sampleEntries = listOf(
        DiaryEntryItem(1, "Hari yang Cerah", "Pagi ini aku bangun dengan...", "😊", "15 Juni 2025"),
        DiaryEntryItem(2, "Sedikit Resah", "Ada beberapa hal yang mengganggu...", "😟", "14 Juni 2025")
    )
    val state = DiaryUiState(isLoading = false, entries = sampleEntries)
    DearDiaryTheme {
        DiaryScreen(state, {}, {}, {}, {}, onRetry = {}, onClearError = {})
    }
}

@Preview(showBackground = true, name = "Layar Kosong (Empty State)")
@Composable
private fun DiaryScreenPreview_Empty() {
    val state = DiaryUiState(isLoading = false, entries = emptyList())
    DearDiaryTheme {
        DiaryScreen(state, {}, {}, {}, {}, onRetry = {}, onClearError = {})
    }
}

@Preview(showBackground = true, name = "Layar Saat Loading Awal")
@Composable
private fun DiaryScreenPreview_Loading() {
    val state = DiaryUiState(isLoading = true, entries = emptyList())
    DearDiaryTheme {
        DiaryScreen(state, {}, {}, {}, {}, onRetry = {}, onClearError = {})
    }
}
