package com.psy.deardiary.features.home

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi // IMPORT BARU
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.deardiary.data.model.ChatMessage
import com.psy.deardiary.features.home.components.*
import com.psy.deardiary.features.home.emojiOptions

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class) // ANOTASI BARU
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCrisisSupport: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    chatViewModel: HomeChatViewModel = hiltViewModel()
) {
    val messages by chatViewModel.messages.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hari Ini Untukmu") },
                actions = {
                    IconButton(onClick = onNavigateToCrisisSupport) {
                        Icon(Icons.Outlined.CrisisAlert, contentDescription = "Dukungan Krisis")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Pengaturan")
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(onSend = { chatViewModel.sendMessage(it) })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    WelcomeCard(
                        timeOfDay = uiState.timeOfDay,
                        userName = uiState.userName,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        state = listState,
                    ) {
                        items(uiState.feedItems, key = { it.hashCode() }) { item ->
                            when (item) {
                                is FeedItem.JournalItem -> JournalItemCard(item.journalEntry)
                                is FeedItem.ArticleSuggestionItem -> ArticleSuggestionCard(item.article)
                            }
                        }
                        items(messages, key = { it.id }) { msg ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically { it / 2 },
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                ChatBubble(
                                    message = msg,
                                    modifier = Modifier.animateItemPlacement() // PENGGUNAAN animateItemPlacement
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ChatInputBar(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showEmojiSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showEmojiSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEmojiSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                emojiOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                text += option.emoji
                                showEmojiSheet = false
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.emoji,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Text(option.label)
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ketik pesan...") },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            )
        )
        IconButton(onClick = { showEmojiSheet = true }) {
            Icon(Icons.Outlined.EmojiEmotions, contentDescription = "Emoji")
        }

        IconButton(
            onClick = {
                onSend(text)
                keyboardController?.hide()
                text = ""
            },
            enabled = text.isNotBlank()
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim")
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        // Remove old dot-based placeholder animation in favor of TypingIndicator

        val bubbleColor = when {
            message.isPlaceholder -> Color.Gray.copy(alpha = 0.5f)
            message.isUser -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

        Surface(
            color = bubbleColor,
            shape = MaterialTheme.shapes.medium
        ) {
            if (message.isPlaceholder) {
                TypingIndicator(modifier = Modifier.padding(8.dp))
            } else {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}