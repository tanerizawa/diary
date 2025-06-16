// File: app/src/main/java/com/psy/deardiary/features/media/MediaViewModel.kt
// Deskripsi: ViewModel untuk mengelola data dan state pada MediaScreen.

package com.psy.deardiary.features.media.presentation

import androidx.lifecycle.ViewModel
import com.psy.deardiary.utils.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// Data class untuk setiap item di UI
data class GuidedJournalPrompt(val title: String, val prompt: String)
data class Playlist(val title: String, val description: String, val color: Long, val audioUrl: String)
data class Article(val title: String, val source: String, val imageUrl: String)

// State untuk MediaScreen, termasuk state pemutaran audio
data class MediaUiState(
    val guidedPrompt: GuidedJournalPrompt,
    val playlists: List<Playlist>,
    val articles: List<Article>,
    val currentlyPlayingUrl: String? = null
)

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        MediaUiState(
            guidedPrompt = GuidedJournalPrompt(
                title = "Refleksi Rasa Syukur",
                prompt = "Tuliskan tiga hal yang kamu syukuri hari ini, sekecil apapun itu..."
            ),
            // Menggunakan URL audio dari sumber public domain
            playlists = listOf(
                Playlist("Hujan di Jendela", "Suara menenangkan untuk fokus & rileks", 0xFFa0c4ff, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
                Playlist("Alunan Piano Klasik", "Musik untuk mengurangi kecemasan", 0xFFbdb2ff, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"),
                Playlist("Suara Hutan Tropis", "Meditasi dengan alam", 0xFFcaffbf, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"),
                Playlist("Ombak Pantai", "Tidur lebih nyenyak malam ini", 0xFF9bf6ff, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3")
            ),
            articles = listOf(
                Article("Memahami Overthinking", "Psikologi+", "https://placehold.co/600x400/ffadad/000000?text=Artikel"),
                Article("5 Cara Sederhana Meditasi", "HelloSehat", "https://placehold.co/600x400/ffd6a5/000000?text=Artikel"),
                Article("Mengapa Jurnal Baik Untukmu?", "Kompasiana", "https://placehold.co/600x400/fdffb6/000000?text=Artikel")
            )
        )
    )
    val uiState = _uiState.asStateFlow()

    fun playOrStopPlaylist(playlist: Playlist) {
        val url = playlist.audioUrl
        if (audioPlayer.isPlaying(url)) {
            audioPlayer.stop {
                _uiState.update { it.copy(currentlyPlayingUrl = null) }
            }
        } else {
            audioPlayer.play(url) {
                // Callback onCompletion, UI akan diupdate
                _uiState.update { it.copy(currentlyPlayingUrl = null) }
            }
            // Update UI segera untuk menampilkan status 'playing'
            _uiState.update { it.copy(currentlyPlayingUrl = url) }
        }
    }

    override fun onCleared() {
        // Pastikan pemutar berhenti saat ViewModel dihancurkan
        audioPlayer.stop {}
        super.onCleared()
    }
}
