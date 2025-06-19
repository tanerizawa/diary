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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.deardiary.ui.components.ConfirmationDialog
import com.psy.deardiary.ui.components.PermissionDeniedDialog
import com.psy.deardiary.ui.theme.Crisis
import com.psy.deardiary.ui.theme.DearDiaryTheme
import android.content.Intent
import android.net.Uri
import android.provider.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    onBackClick: () -> Unit,
    viewModel: JournalEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) } // State untuk dialog
    var showPermissionDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.onStartRecording()
            } else {
                showPermissionDialog = true
            }
        }
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
                Text(
                    text = "Mood", // Simple heading for mood section
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    com.psy.deardiary.features.home.emojiOptions.forEach { option ->
                        MoodSelector(
                            mood = option.emoji,
                            isSelected = uiState.journalMood == option.emoji,
                            onSelect = { viewModel.updateMood(it) },
                            enabled = !uiState.isRecording && !uiState.isPlayingAudio
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                // ... (Sisa kode UI tidak berubah)
            }

            if (showPermissionDialog) {
                PermissionDeniedDialog(
                    onDismissRequest = { showPermissionDialog = false },
                    onOpenSettings = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    message = "Izin merekam audio diperlukan untuk merekam jurnal suara."
                )
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
    Column {
        Text(
            text = "Jurnal Suara",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isRecording) {
                IconButton(onClick = onStopClick) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop")
                }
                val transition = rememberInfiniteTransition(label = "rec")
                val alpha by transition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(500)),
                    label = "alpha"
                )
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Crisis.copy(alpha = alpha))
                )
            } else {
                IconButton(
                    onClick = onRecordClick,
                    enabled = !isPlaying
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Rekam")
                }
            }

            if (hasRecording) {
                IconButton(onClick = onPlaybackClick) {
                    val icon = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow
                    val desc = if (isPlaying) "Hentikan" else "Putar"
                    Icon(icon, contentDescription = desc)
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
    val background = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled) { onSelect(mood) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mood,
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun JournalEditorScreenPreview() {
    DearDiaryTheme {
        val context = LocalContext.current
        val fakeDao = object : com.psy.deardiary.data.local.JournalDao {
            override suspend fun insertEntry(entry: com.psy.deardiary.data.model.JournalEntry) = 1L
            override fun getAllEntries(userId: Int) = kotlinx.coroutines.flow.flowOf(emptyList<com.psy.deardiary.data.model.JournalEntry>())
            override suspend fun getUnsyncedEntries(userId: Int) = emptyList<com.psy.deardiary.data.model.JournalEntry>()
            override suspend fun markAsSynced(
                localId: Int,
                newRemoteId: Int,
                sentimentScore: Float?,
                keyEmotions: String?
            ) {}
            override suspend fun getEntryByRemoteId(remoteId: Int?, userId: Int) = null
            override suspend fun getEntryById(id: Int) = null
            override suspend fun updateEntry(
                id: Int,
                remoteId: Int?,
                title: String,
                content: String,
                mood: String,
                timestamp: Long,
                tags: List<String>,
                sentimentScore: Float?,
                keyEmotions: String?,
                isSynced: Boolean
            ) {}
            override suspend fun updateLocalEntry(entry: com.psy.deardiary.data.model.JournalEntry) {}
            override suspend fun deleteAllEntries(userId: Int) {}
            override suspend fun getAllEntriesOnce(userId: Int) = emptyList<com.psy.deardiary.data.model.JournalEntry>()
            override suspend fun getLatestMood(userId: Int): String? = null
            override suspend fun deleteEntryByLocalId(localId: Int, userId: Int) {}
        }

        val fakeApi = retrofit2.Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(com.psy.deardiary.data.network.JournalApiService::class.java)

        val userPrefs = com.psy.deardiary.data.datastore.UserPreferencesRepository(context)

        val repo = com.psy.deardiary.data.repository.JournalRepository(fakeApi, fakeDao, userPrefs)

        val vm = JournalEditorViewModel(
            repo,
            com.psy.deardiary.utils.AudioRecorder(context),
            com.psy.deardiary.utils.AudioPlayer(),
            context,
            androidx.lifecycle.SavedStateHandle()
        )

        JournalEditorScreen(onBackClick = {}, viewModel = vm)
    }
}
