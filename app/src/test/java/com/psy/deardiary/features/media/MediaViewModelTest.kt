import com.psy.deardiary.features.media.MediaViewModel
import com.psy.deardiary.features.media.Playlist
import com.psy.deardiary.utils.AudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MediaViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var viewModel: MediaViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        audioPlayer = mock()
        viewModel = MediaViewModel(audioPlayer)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun playPlaylist_updatesState() {
        val playlist = viewModel.uiState.value.playlists.first()
        whenever(audioPlayer.isPlaying(playlist.audioUrl)).thenReturn(false)
        viewModel.playOrStopPlaylist(playlist)
        assertEquals(playlist.audioUrl, viewModel.uiState.value.currentlyPlayingUrl)
        verify(audioPlayer).play(playlist.audioUrl, any())
    }

    @Test
    fun stopPlaylist_clearsState() {
        val playlist = viewModel.uiState.value.playlists.first()
        whenever(audioPlayer.isPlaying(playlist.audioUrl)).thenReturn(true)
        viewModel.playOrStopPlaylist(playlist)
        assertEquals(null, viewModel.uiState.value.currentlyPlayingUrl)
        verify(audioPlayer).stop(any())
    }
}
