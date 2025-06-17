package com.psy.deardiary.features.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.deardiary.features.home.components.ArticleSuggestionCard
import com.psy.deardiary.features.home.components.JournalItemCard
import com.psy.deardiary.features.home.components.PromptCard
import com.psy.deardiary.features.home.components.WelcomeCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCrisisSupport: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isQuickEntryVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hari Ini Untukmu") },
                actions = {
                    IconButton(onClick = onNavigateToCrisisSupport) {
                        Icon(Icons.Outlined.CrisisAlert, "Dukungan Krisis")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, "Pengaturan")
                    }
                }
            )
        },
        /*
         * Tidak ada tombol untuk menulis jurnal panjang di tab Beranda.
         * Pengguna hanya dapat membuat catatan singkat di sini.
         */
        bottomBar = {
            // Komponen ini untuk panel input CATATAN SINGKAT
            QuickEntryInput(
                isVisible = isQuickEntryVisible,
                onActivate = { isQuickEntryVisible = true },
                onSave = { text, mood ->
                    viewModel.saveQuickNote(text, mood)
                    isQuickEntryVisible = false // Tutup input setelah simpan
                },
                onCloseRequest = { isQuickEntryVisible = false } // Tutup jika keyboard hilang
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    items(uiState.feedItems, key = { it.hashCode() }) { item ->
                        when (item) {
                            is FeedItem.WelcomeItem -> WelcomeCard(
                                timeOfDay = item.timeOfDay,
                                userName = item.userName,
                                // INI BAGIAN PALING PENTING:
                                // Tombol ini HANYA mengubah state, tidak navigasi.
                                onQuickEntryClick = { isQuickEntryVisible = true }
                            )
                            is FeedItem.PromptItem -> PromptCard(
                                prompt = item.promptText,
                                onNoteClick = { isQuickEntryVisible = true }
                            )
                            is FeedItem.JournalItem -> JournalItemCard(item.journalEntry)
                            is FeedItem.ArticleSuggestionItem -> ArticleSuggestionCard(item.article)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun QuickEntryInput(
    isVisible: Boolean,
    onActivate: () -> Unit,
    onSave: (String, String) -> Unit,
    onCloseRequest: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("\uD83D\uDE10") }
    var showEmojiPicker by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            focusRequester.requestFocus()
        } else {
            // Membersihkan teks dan pilihan saat panel ditutup
            text = ""
            mood = "\uD83D\uDE10"
        }
    }

    if (!isVisible) {
        Surface(tonalElevation = 3.dp, modifier = Modifier.alpha(0.7f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onActivate() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(mood, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tulis catatan singkat...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Surface(tonalElevation = 3.dp) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showEmojiPicker = true }) {
                    Text(mood)
                }
                DropdownMenu(expanded = showEmojiPicker, onDismissRequest = { showEmojiPicker = false }) {
                    listOf("😀", "🙂", "😐", "😢", "😡").forEach { emoji ->
                        DropdownMenuItem(
                            text = { Text(emoji) },
                            onClick = {
                                mood = emoji
                                showEmojiPicker = false
                            }
                        )
                    }
                }
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Tulis catatan singkat...") },
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        onSave(text, mood)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    enabled = text.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Simpan Catatan")
                }
            }
        }
    }
}