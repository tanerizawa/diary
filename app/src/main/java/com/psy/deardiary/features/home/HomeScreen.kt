package com.psy.deardiary.features.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.CrisisAlert
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
import com.psy.deardiary.features.home.components.ArticleSuggestionCard
import com.psy.deardiary.features.home.components.JournalItemCard
import com.psy.deardiary.features.home.components.PromptCard
import com.psy.deardiary.features.home.components.WelcomeCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToCrisisSupport: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    chatViewModel: HomeChatViewModel = hiltViewModel()
) {
    val messages by chatViewModel.messages.collectAsState()

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
        bottomBar = {
            ChatInputBar(onSend = { chatViewModel.sendMessage(it) })
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
                            )
                            is FeedItem.PromptItem -> PromptCard(prompt = item.promptText)
                            is FeedItem.JournalItem -> JournalItemCard(item.journalEntry)
                            is FeedItem.ArticleSuggestionItem -> ArticleSuggestionCard(item.article)
                        }
                    }
                    items(messages, key = { it.hashCode() }) { msg ->
                        ChatBubble(msg)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ChatInputBar(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
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
private fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}