import com.psy.deardiary.data.model.ChatMessage
import com.psy.deardiary.data.repository.ChatRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.features.home.HomeChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeChatViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: ChatRepository
    private lateinit var viewModel: HomeChatViewModel
    private lateinit var conversationFlow: MutableStateFlow<List<ChatMessage>>

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
        conversationFlow = MutableStateFlow(emptyList())
        whenever(repository.getConversation()).thenReturn(conversationFlow)
        viewModel = HomeChatViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sendMessage_delegatesToRepository() = runTest {
        whenever(repository.addMessage(any(), eq(true), eq(false))).thenReturn(ChatMessage(text="hi", isUser=true, userId = 1))
        whenever(repository.addMessage(any(), eq(false), eq(true))).thenReturn(ChatMessage(id=2, text="placeholder", isUser=false, isPlaceholder=true, userId = 1))
        whenever(repository.fetchReply("hi")).thenReturn(Result.Success("hello"))
        viewModel.sendMessage("hi")
        advanceUntilIdle()
        verify(repository).addMessage("hi", true, false)
        verify(repository).addMessage("Sedang mengetik jawaban...", false, true)
        verify(repository).fetchReply("hi")
        verify(repository).replaceMessage(2, "hello")
    }

    @Test
    fun collectsConversation_updatesMessages() = runTest {
        val msg = ChatMessage(id = 1, text = "hello", isUser = false, userId = 1)
        conversationFlow.value = listOf(msg)
        advanceUntilIdle()
        assertEquals(listOf(msg), viewModel.messages.value)
    }
}
