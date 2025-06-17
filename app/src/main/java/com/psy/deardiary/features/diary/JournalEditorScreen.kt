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
import com.psy.deardiary.ui.components.ConfirmationDialog
import com.psy.deardiary.ui.theme.Crisis
import com.psy.deardiary.ui.theme.DearDiaryTheme

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
                // ... (Sisa kode UI tidak berubah)
            }
        }
    }
}

// ... (Sisa file tidak berubah)
@Composable
private fun VoiceJournalSection( isRecording: Boolean, hasRecording: Boolean, isPlaying: Boolean, onRecordClick: () -> Unit, onStopClick: () -> Unit, onPlaybackClick: () -> Unit ) { /* ... */ }

@Composable
private fun MoodSelector( mood: String, isSelected: Boolean, onSelect: (String) -> Unit, enabled: Boolean ) { /* ... */ }

@Preview(showBackground = true)
@Composable
private fun JournalEditorScreenPreview() { /* ... */ }
