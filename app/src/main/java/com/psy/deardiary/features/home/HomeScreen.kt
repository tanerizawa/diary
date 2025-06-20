package com.psy.deardiary.features.home

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi // IMPORT BARU
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CrisisAlert
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.deardiary.data.model.ChatMessage
import com.psy.deardiary.features.home.components.TypingIndicator
import com.psy.deardiary.features.home.emojiOptions
import com.psy.deardiary.features.home.components.JournalItemCard
import com.psy.deardiary.features.home.components.ArticleSuggestionCard
import com.psy.deardiary.features.home.components.ChatPromptCard
import com.psy.deardiary.features.home.FeedItem
import com.psy.deardiary.ui.components.ConfirmationDialog
import com.psy.deardiary.utils.playNotificationFeedback

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class) // ANOTASI BARU
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCrisisSupport: () -> Unit,
    onNavigateToEditor: () -> Unit,
    onNavigateToEditorWithPrompt: (String) -> Unit,
    mainViewModel: com.psy.deardiary.features.main.MainViewModel,
    viewModel: HomeViewModel = hiltViewModel(),
    chatViewModel: HomeChatViewModel = hiltViewModel()
) {
    val messages by chatViewModel.messages.collectAsState()
    val sentimentScore by chatViewModel.latestSentiment.collectAsState(initial = null)
    val chatUiState by chatViewModel.uiState.collectAsState()
    val selectedIds by chatViewModel.selectedIds.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val feedItems by viewModel.feedItems.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var previousAiCount by remember { mutableStateOf<Int?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(chatUiState.errorMessage) {
        chatUiState.errorMessage?.let {
            mainViewModel.showError(it)
            chatViewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(messages, feedItems) {
        if (messages.isNotEmpty()) {
            val offset = feedItems.size
            listState.animateScrollToItem(offset + messages.lastIndex)
        }
    }

    LaunchedEffect(messages) {
        val aiCount = messages.count { !it.isUser && !it.isPlaceholder }
        previousAiCount?.let { prev ->
            if (aiCount > prev) {
                playNotificationFeedback(context)
            }
        }
        previousAiCount = aiCount
    }

    Scaffold(
        topBar = {
            if (selectedIds.isNotEmpty()) {
                TopAppBar(
                    title = { Text("${selectedIds.size} dipilih") },
                    navigationIcon = {
                        IconButton(onClick = { chatViewModel.clearSelection() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Batal")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Hapus")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(composeGreeting(uiState.timeOfDay, uiState.userName, uiState.lastMood, sentimentScore)) },
                    actions = {
                        IconButton(onClick = onNavigateToCrisisSupport) {
                            Icon(Icons.Outlined.CrisisAlert, contentDescription = "Dukungan Krisis")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Pengaturan")
                        }
                    }
                )
            }
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
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    state = listState,
                ) {
                    items(feedItems) { item ->
                        when (item) {
                            is FeedItem.JournalItem -> JournalItemCard(item.journalEntry)
                            is FeedItem.ArticleSuggestionItem -> ArticleSuggestionCard(item.article)
                            is FeedItem.ChatPromptItem -> ChatPromptCard(item.message)
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
                                isSelected = selectedIds.contains(msg.id),
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .combinedClickable(
                                        onClick = {
                                            if (selectedIds.isNotEmpty()) chatViewModel.toggleSelection(msg.id)
                                        },
                                        onLongClick = { chatViewModel.toggleSelection(msg.id) }
                                    )
                            )
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                ConfirmationDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    onConfirm = { chatViewModel.deleteSelectedMessages() },
                    title = "Hapus Pesan",
                    text = "Hapus pesan yang dipilih?"
                )
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
internal fun ChatBubble(
    message: ChatMessage,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        // Remove old dot-based placeholder animation in favor of TypingIndicator

        // Base colors depending on sender
        val baseColor = if (message.isUser) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }

        val bubbleColor = if (message.isPlaceholder) {
            Color.Gray.copy(alpha = 0.5f)
        } else {
            baseColor
        }

        val contentColor = if (message.isUser) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }

        Surface(
            color = bubbleColor,
            contentColor = contentColor,
            shape = MaterialTheme.shapes.medium,
            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            if (message.isPlaceholder) {
                TypingIndicator(modifier = Modifier.padding(8.dp))
            } else {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = message.text)
                    // Sentiment display disabled but data kept for statistics
                    /*
                    if (message.sentimentScore != null || message.keyEmotions != null) {
                        Spacer(Modifier.height(4.dp))
                        val score = message.sentimentScore
                        val (icon, color) = when {
                            score == null -> Pair(Icons.Outlined.SentimentNeutral, MaterialTheme.colorScheme.onSurfaceVariant)
                            score >= 0f -> Pair(Icons.Outlined.Mood, Color(0xFF2E7D32))
                            else -> Pair(Icons.Outlined.MoodBad, Color(0xFFC62828))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(16.dp)
                            )
                            score?.let {
                                Text(
                                    text = String.format("%.1f", it),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = color,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            message.keyEmotions?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    */
                }
            }
        }
    }
}

private fun composeGreeting(
    timeOfDay: String,
    name: String,
    lastMood: String?,
    sentiment: Float?,
): String {
    return "$timeOfDay, $name."
}
}
