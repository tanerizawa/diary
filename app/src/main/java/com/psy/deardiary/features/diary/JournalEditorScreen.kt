// VERSI DIPERBARUI: Menghapus LaunchedEffect yang memanggil fungsi private.

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.deardiary.ui.theme.Crisis
import com.psy.deardiary.ui.theme.DearDiaryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    onBackClick: () -> Unit,
    viewModel: JournalEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.onStartRecording()
            } else {
                // TODO: Tampilkan pesan kepada pengguna bahwa izin ditolak
            }
        }
    )

    // PERBAIKAN: Menghapus LaunchedEffect(entryId) karena logika sudah pindah ke init ViewModel.
    // Kode ini tidak lagi diperlukan di sini.

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBackClick()
            viewModel.onSaveComplete()
        }
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
                    enabled = !uiState.isRecording
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.journalContent,
                    onValueChange = { viewModel.updateContent(it) },
                    label = { Text("Apa yang kamu rasakan hari ini?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    ),
                    enabled = !uiState.isRecording
                )
                Spacer(modifier = Modifier.height(24.dp))

                VoiceJournalSection(
                    isRecording = uiState.isRecording,
                    hasRecording = uiState.voiceNotePath != null,
                    onRecordClick = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onStopClick = { viewModel.onStopRecording() }
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
                            onSelect = { selectedMood -> viewModel.updateMood(selectedMood) },
                            enabled = !uiState.isRecording
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceJournalSection(
    isRecording: Boolean,
    hasRecording: Boolean,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (isRecording) {
            Button(
                onClick = onStopClick,
                colors = ButtonDefaults.buttonColors(containerColor = Crisis)
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Hentikan Rekaman")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hentikan Rekaman")
            }
        } else {
            OutlinedButton(
                onClick = onRecordClick,
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Rekam Suara")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if(hasRecording) "Rekam Ulang Suara" else "Jurnal Suara")
            }
        }

        AnimatedVisibility(visible = isRecording || hasRecording) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                if (isRecording) {
                    val infiniteTransition = rememberInfiniteTransition(label = "blinking_dot")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(500),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ), label = "blinking_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Crisis.copy(alpha = alpha))
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = "Rekaman Tersimpan", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if(isRecording) "Merekam..." else "Rekaman tersimpan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    val color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    TextButton(
        onClick = { onSelect(mood) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = color
        ),
        enabled = enabled
    ) {
        Text(text = mood, style = MaterialTheme.typography.headlineLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun JournalEditorScreenPreview() {
    DearDiaryTheme {
        JournalEditorScreen(onBackClick = {})
    }
}
