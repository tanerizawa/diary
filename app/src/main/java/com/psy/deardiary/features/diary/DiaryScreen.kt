// LOKASI: app/src/main/java/com/psy/deardiary/features/diary/DiaryScreen.kt

package com.psy.deardiary.features.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.ui.components.AnimatedFadeIn
import com.psy.deardiary.ui.components.NetworkErrorSnackbar
import com.psy.deardiary.ui.theme.DearDiaryTheme
import java.text.SimpleDateFormat
import java.util.*

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
                state.isLoading && state.entries.isEmpty() -> {
                    CircularProgressIndicator()
                }
                state.entries.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = state.entries, key = { it.id }) { entry ->
                            AnimatedFadeIn {
                                // PERBAIKAN: Memanggil JournalCard yang sudah diperbarui
                                JournalCard(
                                    entry = entry, // Langsung meneruskan objek JournalEntry
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
fun JournalCard(entry: JournalEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.mood,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.Top)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.title.ifBlank { "Tanpa Judul" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("id", "ID"))
                            .format(Date(entry.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                    if (!entry.keyEmotions.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            entry.keyEmotions.split(",").take(3).forEach { emotion ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(emotion.trim().replaceFirstChar { it.uppercase() }) },
                                )
                            }
                        }
                    }
                }
                entry.sentimentScore?.let { score ->
                    Spacer(modifier = Modifier.width(12.dp))
                    val sentimentColor = when {
                        score > 0.15 -> Color(0xFF4CAF50) // Positif
                        score < -0.15 -> Color(0xFFF44336) // Negatif
                        else -> MaterialTheme.colorScheme.outline
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(sentimentColor)
                            .align(Alignment.Top)
                    )
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
        Text(text = "📖", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Ruang Amanmu Menanti", style = MaterialTheme.typography.titleLarge)
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
    // PERBAIKAN: Data sampel sekarang menggunakan JournalEntry secara langsung
    val sampleEntries = listOf(
        JournalEntry(
            id = 1,
            title = "Hari yang Cerah",
            content = "Pagi ini aku bangun dengan perasaan yang sangat ringan. Entah kenapa, semua terasa mungkin...",
            mood = "😊",
            timestamp = System.currentTimeMillis(),
            sentimentScore = 0.8f, // Menggunakan 'f' untuk Float
            keyEmotions = "syukur, optimis",
            tags = emptyList() // Memberikan nilai untuk 'tags'
        ),
        JournalEntry(
            id = 2,
            title = "Sedikit Resah",
            content = "Ada beberapa hal yang mengganggu pikiranku hari ini. Pekerjaan menumpuk dan aku merasa sedikit kewalahan.",
            mood = "😟",
            timestamp = System.currentTimeMillis() - 86400000,
            sentimentScore = -0.5f,
            keyEmotions = "cemas, lelah",
            tags = emptyList()
        )
    )
    val state = DiaryUiState(isLoading = false, entries = sampleEntries)
    DearDiaryTheme {
        DiaryScreen(state, {}, {}, {}, {}, onRetry = {}, onClearError = {})
    }
}

@Preview(showBackground = true, name = "Layar Kosong")
@Composable
private fun DiaryScreenPreview_Empty() {
    // PERBAIKAN: Menggunakan List<JournalEntry> yang kosong
    val state = DiaryUiState(isLoading = false, entries = emptyList())
    DearDiaryTheme {
        DiaryScreen(state, {}, {}, {}, {}, onRetry = {}, onClearError = {})
    }
}