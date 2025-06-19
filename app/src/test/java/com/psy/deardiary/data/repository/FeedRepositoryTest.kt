import com.psy.deardiary.data.repository.FeedRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.dto.FeedItemResponse
import com.psy.deardiary.data.network.FeedApiService
import com.psy.deardiary.features.home.FeedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FeedRepositoryTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var api: FeedApiService
    private lateinit var repository: FeedRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        api = mock()
        repository = FeedRepository(api)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getFeedSuccess_returnsItems() = runTest {
        val item = FeedItemResponse("chat_prompt", null, null, "hello")
        whenever(api.getFeed()).thenReturn(Response.success(listOf(item)))

        val result = repository.getFeed()

        assertTrue(result is Result.Success)
        assertEquals(1, (result as Result.Success).data.size)
        assertTrue(result.data.first() is FeedItem.ChatPromptItem)
    }

    @Test
    fun getFeedError_returnsError() = runTest {
        whenever(api.getFeed()).thenReturn(Response.error(500, "".toResponseBody(null)))

        val result = repository.getFeed()

        assertTrue(result is Result.Error)
    }
}
