import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.features.growth.GrowthViewModel
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
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class GrowthViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val journalFlow = MutableStateFlow<List<JournalEntry>>(emptyList())
    private lateinit var repository: JournalRepository
    private lateinit var viewModel: GrowthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
        whenever(repository.journals).thenReturn(journalFlow)
        viewModel = GrowthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emitEntries_updatesTotals() = runTest {
        val entry1 = JournalEntry(title="a", content="c", mood="😊", tags=emptyList())
        val entry2 = JournalEntry(title="b", content="d", mood="😊", tags=emptyList())
        journalFlow.value = listOf(entry1, entry2)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.totalJournals)
    }
}
