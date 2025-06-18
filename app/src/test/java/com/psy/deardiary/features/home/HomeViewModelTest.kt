import com.psy.deardiary.features.home.HomeViewModel
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.data.repository.UserRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var journalRepo: JournalRepository
    private lateinit var userRepo: UserRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        journalRepo = mock()
        userRepo = mock()
        whenever(userRepo.getProfile()).thenReturn(Result.Success(UserProfile(1, "e", "Budi", null)))
        whenever(journalRepo.getLatestMood()).thenReturn("\uD83D\uDE22")
        viewModel = HomeViewModel(journalRepo, userRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initializesGreeting() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state.timeOfDay.isNotBlank())
        assertEquals("Budi", state.userName)
        assertEquals("\uD83D\uDE22", state.lastMood)
    }
}
