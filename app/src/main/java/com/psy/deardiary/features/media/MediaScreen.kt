// File: app/src/main/java/com/psy/deardiary/features/media/MediaScreen.kt
// VERSI DIPERBARUI: Menambahkan semua impor yang diperlukan untuk mengatasi error.

package com.psy.deardiary.features.media

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.psy.deardiary.ui.theme.DearDiaryTheme

// PERBAIKAN: Menambahkan impor untuk data class dan ViewModel
import com.psy.deardiary.features.media.Article
import com.psy.deardiary.features.media.GuidedJournalPrompt
import com.psy.deardiary.features.media.Playlist
import com.psy.deardiary.features.media.MediaViewModel

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuidedJournalSection(
    prompt: GuidedJournalPrompt,
    onStartWriting: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader("Jurnal Terpandu Hari Ini")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = prompt.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = prompt.prompt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onStartWriting) {
                    Text("Mulai Menulis")
                }
            }
        }
    }
}

@Composable
private fun PlaylistCard(
    playlist: Playlist,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.size(160.dp, 200.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(playlist.color)),
            contentAlignment = Alignment.BottomStart
        ) {
            if (isPlaying) {
                val transition = rememberInfiniteTransition(label = "playing_animation")
                val alpha by transition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000),
                        repeatMode = RepeatMode.Reverse
                    ), label = "alpha"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = alpha))
                )
            }
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = playlist.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = playlist.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Stop" else "Play",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(4.dp),
                tint = Color.White
            )
        }
    }
}


@Composable
private fun RelaxationMusicSection(
    playlists: List<Playlist>,
    currentlyPlayingUrl: String?,
    onPlaylistClick: (Playlist) -> Unit
) {
    Column {
        SectionHeader("Musik Relaksasi")
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(playlists) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    isPlaying = playlist.audioUrl == currentlyPlayingUrl,
                    onClick = { onPlaylistClick(playlist) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleCard(article: Article, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { /* TODO: Buka detail artikel */ }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = "Gambar Artikel",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sumber: ${article.source}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Baca Artikel",
                modifier = Modifier.padding(end = 12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaScreen(
    onNavigateToEditor: () -> Unit,
    onNavigateToEditorWithPrompt: (String) -> Unit,
    viewModel: MediaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Media & Inspirasi") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToEditor) {
                Icon(Icons.Default.Edit, contentDescription = "Tulis Jurnal Panjang")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                GuidedJournalSection(
                    prompt = uiState.guidedPrompt,
                    onStartWriting = { onNavigateToEditorWithPrompt(uiState.guidedPrompt.prompt) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                RelaxationMusicSection(
                    playlists = uiState.playlists,
                    currentlyPlayingUrl = uiState.currentlyPlayingUrl,
                    onPlaylistClick = { viewModel.playOrStopPlaylist(it) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                SectionHeader("Artikel untuk Anda")
            }
            items(uiState.articles) { article ->
                ArticleCard(
                    article = article,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MediaScreenPreview() {
    DearDiaryTheme {
        MediaScreen(onNavigateToEditor = {}, onNavigateToEditorWithPrompt = {})
    }
}
