// LOKASI: app/src/main/java/com/psy/deardiary/features/diary/JournalEditorScreen.kt

package com.psy.deardiary.features.diary

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.compose.ui.platform.LocalContext
import com.psy.deardiary.data.local.JournalDao
import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.data.network.JournalApiService
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.ui.components.ConfirmationDialog
import com.psy.deardiary.ui.theme.Crisis
import com.psy.deardiary.ui.theme.DearDiaryTheme
import com.psy.deardiary.utils.AudioPlayer
import com.psy.deardiary.utils.AudioRecorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    onBackClick: () -> Unit,
    viewModel: JournalEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) } // State untuk dialog

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> if (isGranted) viewModel.onStartRecording() }
    )

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBackClick()
            viewModel.onSaveComplete()
        }
    }

    // PENAMBAHAN: LaunchedEffect untuk navigasi setelah hapus
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onBackClick()
            viewModel.onDeleteComplete()
        }
    }

    // PENAMBAHAN: Tampilkan dialog konfirmasi hapus
    if (showDeleteDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteJournal()
                showDeleteDialog = false
            },
            title = "Hapus Jurnal",
            text = "Anda yakin ingin menghapus entri jurnal ini secara permanen?",
            confirmText = "Ya, Hapus"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.entryId == null) "Jurnal Baru" else "Edit Jurnal") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveJournal() },
                        enabled = !uiState.isLoading && !uiState.isRecording
                    ) {
                        Icon(Icons.Default.Save, "Simpan Jurnal")
                    }
                    // PENAMBAHAN: Tombol hapus, hanya tampil saat mengedit
                    if (uiState.entryId != null) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = !uiState.isLoading
                        ) {
                            Icon(Icons.Default.Delete, "Hapus Jurnal")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
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
                    singleLine = true,
                    enabled = !uiState.isRecording && !uiState.isPlayingAudio
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.journalContent,
                    onValueChange = { viewModel.updateContent(it) },
                    label = { Text("Apa yang kamu rasakan hari ini?") },
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    enabled = !uiState.isRecording && !uiState.isPlayingAudio
                )
                Spacer(modifier = Modifier.height(24.dp))
                VoiceJournalSection(
                    isRecording = uiState.isRecording,
                    hasRecording = uiState.voiceNotePath != null,
                    isPlaying = uiState.isPlayingAudio,
                    onRecordClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onStopClick = { viewModel.onStopRecording() },
                    onPlaybackClick = { viewModel.onPlaybackClicked() }
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Pilihan mood
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val moods = listOf("😊", "😐", "😟", "😠", "😢")
                    moods.forEach { mood ->
                        MoodSelector(
                            mood = mood,
                            isSelected = uiState.journalMood == mood,
                            onSelect = { viewModel.updateMood(it) },
                            enabled = !uiState.isRecording && !uiState.isPlayingAudio
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ... (Sisa file tidak berubah)

@Composable
private fun VoiceJournalSection(
    isRecording: Boolean,
    hasRecording: Boolean,
    isPlaying: Boolean,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    onPlaybackClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isRecording) {
            IconButton(onClick = onStopClick) {
                Icon(Icons.Default.Stop, contentDescription = "Stop Recording")
            }
        } else {
            IconButton(onClick = onRecordClick) {
                Icon(Icons.Default.Mic, contentDescription = "Start Recording")
            }
        }

        if (hasRecording) {
            IconButton(onClick = onPlaybackClick) {
                if (isPlaying) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop Playback")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Recording")
                }
            }
        }
    }
}

@Composable
private fun MoodSelector(
    mood: String,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    enabled: Boolean
) {
    val background = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled) { onSelect(mood) },
        contentAlignment = Alignment.Center
    ) {
        Text(text = mood, color = contentColor, style = MaterialTheme.typography.titleLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun JournalEditorScreenPreview() {
    DearDiaryTheme {
        // Membuat instance ViewModel palsu untuk preview
        val context = LocalContext.current
        val fakeRepository = JournalRepository(
            journalApiService = Retrofit.Builder()
                .baseUrl("http://localhost/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JournalApiService::class.java),
            journalDao = object : JournalDao {
                override suspend fun insertEntry(entry: JournalEntry): Long = 0
                override fun getAllEntries(): Flow<List<JournalEntry>> = flowOf(emptyList())
                override suspend fun getUnsyncedEntries(): List<JournalEntry> = emptyList()
                override suspend fun markAsSynced(localId: Int, newRemoteId: Int) {}
                override suspend fun getEntryByRemoteId(remoteId: Int?): JournalEntry? = null
                override suspend fun getEntryById(id: Int): JournalEntry? = null
                override suspend fun updateEntry(id: Int, remoteId: Int?, title: String, content: String, mood: String, timestamp: Long, tags: List<String>, isSynced: Boolean) {}
                override suspend fun updateLocalEntry(entry: JournalEntry) {}
                override suspend fun deleteAllEntries() {}
                override suspend fun getAllEntriesOnce(): List<JournalEntry> = emptyList()
                override suspend fun deleteEntryByLocalId(localId: Int) {}
                override suspend fun upsertAll(entries: List<JournalEntry>) {}
            }
        )

        val fakeViewModel = JournalEditorViewModel(
            journalRepository = fakeRepository,
            audioRecorder = AudioRecorder(context),
            audioPlayer = AudioPlayer(),
            context = context,
            savedStateHandle = SavedStateHandle()
        )

        JournalEditorScreen(onBackClick = {}, viewModel = fakeViewModel)
    }
}
