import com.psy.deardiary.data.repository.AuthRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.features.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
        viewModel = AuthViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginSuccess_updatesState() = runTest {
        whenever(repository.login(any(), any())).thenReturn(Result.Success(Unit))
        viewModel.login("a@a.com", "pass")
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.isLoginSuccess)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun loginError_setsErrorMessage() = runTest {
        whenever(repository.login(any(), any())).thenReturn(Result.Error("oops"))
        viewModel.login("a@a.com", "pass")
        advanceUntilIdle()
        assertEquals("oops", viewModel.uiState.value.errorMessage)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }
}
