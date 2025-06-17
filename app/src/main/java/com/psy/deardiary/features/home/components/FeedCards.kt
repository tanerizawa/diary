package com.psy.deardiary.features.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.features.media.Article
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WelcomeCard(
    timeOfDay: String,
    userName: String,
    onQuickEntryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "$timeOfDay, $userName.", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Bagaimana perasaanmu saat ini?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onQuickEntryClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tulis Catatan Singkat")
            }
        }
    }
}

@Composable
fun PromptCard(prompt: String, onNoteClick: () -> Unit) {
    OutlinedCard(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💡 Prompt Untukmu", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(prompt, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            FilledTonalButton(onClick = onNoteClick) {
                Text("Tulis Catatan Singkat")
            }
        }
    }
}

@Composable
fun JournalItemCard(entry: JournalEntry) {
    if (entry.title.isNotBlank()) {
        FullJournalCard(entry)
    } else {
        QuickNoteCard(entry)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullJournalCard(entry: JournalEntry) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp), onClick = {}) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(entry.title, style = MaterialTheme.typography.titleLarge)
            Text(
                SimpleDateFormat("EEEE, d MMMM yyyy HH:mm", Locale("id", "ID")).format(Date(entry.timestamp)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(entry.content, style = MaterialTheme.typography.bodyMedium, maxLines = 4, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickNoteCard(entry: JournalEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = {}
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.mood.isNotBlank()) {
                Text(
                    text = entry.mood,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(end = 12.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.SentimentNeutral,
                    contentDescription = "Catatan Singkat",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.content, style = MaterialTheme.typography.bodyLarge)
                Text(
                    SimpleDateFormat("d MMM, HH:mm", Locale("id", "ID")).format(Date(entry.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ArticleSuggestionCard(article: Article) {
    OutlinedCard(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("📖 Mungkin kamu tertarik", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(article.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text("Sumber: ${article.source}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}