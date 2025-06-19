import com.psy.deardiary.data.repository.ChatRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.local.ChatMessageDao
import com.psy.deardiary.data.network.ChatApiService
import com.psy.deardiary.data.dto.AiChatResponse
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var api: ChatApiService
    private lateinit var dao: ChatMessageDao
    private lateinit var prefs: UserPreferencesRepository
    private lateinit var repository: ChatRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        api = mock()
        dao = mock()
        prefs = mock()
        repository = ChatRepository(api, dao, prefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchReplySuccess_returnsSuccess() = runTest {
        whenever(api.sendMessage(any())).thenReturn(Response.success(AiChatResponse("hi")))

        val result = repository.fetchReply("hello")

        assertTrue(result is Result.Success)
    }

    @Test
    fun fetchReplyError_returnsError() = runTest {
        whenever(api.sendMessage(any())).thenReturn(Response.error(500, "".toResponseBody(null)))

        val result = repository.fetchReply("hello")

        assertTrue(result is Result.Error)
    }
}
