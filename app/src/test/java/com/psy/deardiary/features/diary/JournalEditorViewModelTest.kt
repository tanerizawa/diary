import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.features.diary.JournalEditorViewModel
import com.psy.deardiary.utils.AudioPlayer
import com.psy.deardiary.utils.AudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class JournalEditorViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: JournalRepository
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var viewModel: JournalEditorViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
        audioRecorder = mock()
        audioPlayer = mock()
        val context = mock<Context>()
        viewModel = JournalEditorViewModel(repository, audioRecorder, audioPlayer, context, SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveJournal_success_setsIsSaved() = runTest {
        whenever(repository.createJournal("", "", "😊", null)).thenReturn(Result.Success(Unit))
        viewModel.saveJournal()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSaved)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
