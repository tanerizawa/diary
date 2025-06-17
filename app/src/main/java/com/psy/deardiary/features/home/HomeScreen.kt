package com.psy.deardiary.features.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
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
    onNavigateToEditor: () -> Unit,
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
        floatingActionButton = {
            // FAB ini untuk membuka editor JURNAL LENGKAP
            FloatingActionButton(onClick = onNavigateToEditor) {
                Icon(Icons.Default.Edit, "Tulis Jurnal Lengkap")
            }
        },
        bottomBar = {
            // Komponen ini untuk panel input CATATAN SINGKAT
            QuickEntryInput(
                isVisible = isQuickEntryVisible,
                onSave = { text ->
                    viewModel.saveQuickNote(text)
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
                                onWriteClick = onNavigateToEditor
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
    onSave: (String) -> Unit,
    onCloseRequest: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            focusRequester.requestFocus()
        } else {
            // Membersihkan teks saat panel ditutup
            text = ""
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        Surface(tonalElevation = 3.dp) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp).navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        onSave(text)
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