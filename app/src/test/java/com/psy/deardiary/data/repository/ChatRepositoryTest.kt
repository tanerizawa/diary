import com.psy.deardiary.data.repository.ChatRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.local.ChatMessageDao
import com.psy.deardiary.data.dto.ChatMessageResponse
import com.psy.deardiary.data.model.ChatMessage
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import kotlinx.coroutines.flow.flowOf
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
        whenever(api.sendMessage(any())).thenReturn(
            Response.success(AiChatResponse("balas_teks", "hi", replyId = 1))
        )

        val result = repository.fetchReply("hello")

        assertTrue(result is Result.Success)
    }

    @Test
    fun fetchReplyError_returnsError() = runTest {
        whenever(api.sendMessage(any())).thenReturn(Response.error(500, "".toResponseBody(null)))

        val result = repository.fetchReply("hello")

        assertTrue(result is Result.Error)
    }

    @Test
    fun syncPendingMessages_duplicateRemote_skipsLocalUpdate() = runTest {
        val unsynced = ChatMessage(text = "hi", isUser = true, userId = 1)
        whenever(prefs.userId).thenReturn(flowOf(1))
        whenever(dao.getUnsyncedMessages(1)).thenReturn(listOf(unsynced.copy(id = 1)))
        whenever(api.postMessage(any())).thenReturn(
            Response.success(ChatMessageResponse(2, "hi", true, 0L, 1, null, null, null))
        )
        whenever(dao.getMessageByRemoteId(2, 1)).thenReturn(unsynced.copy(id = 2, remoteId = 2, isSynced = true))

        val result = repository.syncPendingMessages()

        assertTrue(result is Result.Success)
        verify(dao).deleteMessages(listOf(1), 1)
        verify(dao, never()).markAsSynced(any(), any(), any(), any(), any())
    }
}
