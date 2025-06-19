import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.dto.JournalResponse
import com.psy.deardiary.data.local.JournalDao
import com.psy.deardiary.data.network.JournalApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JournalRepositoryTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var api: JournalApiService
    private lateinit var dao: JournalDao
    private lateinit var prefs: UserPreferencesRepository
    private lateinit var repository: JournalRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        api = mock()
        dao = mock()
        prefs = mock()
        whenever(prefs.userId).thenReturn(flowOf(1))
        repository = JournalRepository(api, dao, prefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshSuccess_updatesDao() = runTest {
        val journal = JournalResponse(1, "t", "c", "m", 0L, 1, null, null)
        whenever(api.getJournals()).thenReturn(Response.success(listOf(journal)))

        val result = repository.refreshJournals()

        assertTrue(result is Result.Success)
        verify(dao).upsertAll(any())
    }

    @Test
    fun refreshError_returnsError() = runTest {
        whenever(api.getJournals()).thenReturn(Response.error(500, "".toResponseBody(null)))

        val result = repository.refreshJournals()

        assertTrue(result is Result.Error)
        verify(dao, never()).upsertAll(any())
    }
}
