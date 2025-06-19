import com.psy.deardiary.data.model.ChatMessage
import com.psy.deardiary.data.repository.ChatRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.dto.AiChatResponse
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
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HomeChatViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: ChatRepository
    private lateinit var viewModel: HomeChatViewModel
    private lateinit var conversationFlow: MutableStateFlow<List<ChatMessage>>
    private lateinit var sentimentFlow: MutableStateFlow<Float?>
    private lateinit var messagesFlow: MutableStateFlow<List<ChatMessage>>
    private lateinit var lastPromptFlow: MutableStateFlow<Long?>
    private lateinit var prefRepo: UserPreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
        prefRepo = mock()
        conversationFlow = MutableStateFlow(emptyList())
        sentimentFlow = MutableStateFlow(null)
        messagesFlow = MutableStateFlow(emptyList())
        lastPromptFlow = MutableStateFlow(0L)
        whenever(repository.getConversation()).thenReturn(conversationFlow)
        whenever(repository.latestSentiment).thenReturn(sentimentFlow)
        whenever(repository.messages).thenReturn(messagesFlow)
        whenever(prefRepo.lastAiPrompt).thenReturn(lastPromptFlow)
        whenever(repository.userPreferencesRepository).thenReturn(prefRepo)
        whenever(repository.promptChat()).thenReturn(Result.Success(AiChatResponse("p", null)))
        whenever(repository.refreshMessages()).thenReturn(Result.Success(Unit))
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
        whenever(repository.sendMessage("hi")).thenReturn(
            Result.Success(AiChatResponse("hello", "happy"))
        )
        viewModel.sendMessage("hi")
        advanceUntilIdle()
        verify(repository).addMessage("hi", true, false)
        verify(repository).addMessage("Sedang mengetik jawaban...", false, true)
        verify(repository).sendMessage("hi")
        verify(repository).updateMessageWithReply(2, "hello", "happy")
    }

    @Test
    fun collectsConversation_updatesMessages() = runTest {
        val msg = ChatMessage(id = 1, text = "hello", isUser = false, userId = 1)
        conversationFlow.value = listOf(msg)
        advanceUntilIdle()
        assertEquals(listOf(msg), viewModel.messages.value)
    }

    @Test
    fun refreshMessages_calledOnInit() = runTest {
        advanceUntilIdle()
        verify(repository).refreshMessages()
    }
}
