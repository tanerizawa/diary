import com.psy.deardiary.data.repository.FeedRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.features.home.FeedItem
import com.psy.deardiary.features.home.HomeViewModel
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FeedRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
        whenever(repository.getFeed()).thenReturn(Result.Success(emptyList()))
        viewModel = HomeViewModel(mock(), repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun feedUpdates_whenEntriesEmitted() = runTest {
        whenever(repository.getFeed()).thenReturn(
            Result.Success(listOf(FeedItem.ChatPromptItem("hi")))
        )
        advanceUntilIdle()
        val feed = viewModel.uiState.value.feedItems
        assertEquals(1, feed.size)
        assertTrue(feed[0] is FeedItem.ChatPromptItem)
    }
}
