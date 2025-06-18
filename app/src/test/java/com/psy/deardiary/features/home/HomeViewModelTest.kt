import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.features.home.FeedItem
import com.psy.deardiary.features.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val journalFlow = MutableStateFlow<List<JournalEntry>>(emptyList())
    private lateinit var repository: JournalRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
        whenever(repository.journals).thenReturn(journalFlow)
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun feedUpdates_whenEntriesEmitted() = runTest {
        val entry = JournalEntry(title = "t", content = "saya cemas", mood = "😟", tags = emptyList())
        journalFlow.value = listOf(entry)
        advanceUntilIdle()
        val feed = viewModel.uiState.value.feedItems
        assertEquals(2, feed.size)
        assertTrue(feed[0] is FeedItem.JournalItem)
        assertTrue(feed[1] is FeedItem.ArticleSuggestionItem)
    }
}
