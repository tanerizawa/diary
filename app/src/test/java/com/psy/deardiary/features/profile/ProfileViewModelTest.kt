import com.psy.deardiary.data.model.UserProfile
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.repository.UserRepository
import com.psy.deardiary.features.profile.ProfileViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: UserRepository
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadProfileSuccess_updatesUiState() = runTest {
        whenever(repository.getProfile()).thenReturn(
            Result.Success(UserProfile(1, "e@x.com", "Budi", "Bio"))
        )
        viewModel = ProfileViewModel(repository)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("e@x.com", state.email)
        assertEquals("Budi", state.name)
        assertEquals("Bio", state.bio)
        assertNull(state.message)
    }

    @Test
    fun updateProfileSuccess_appliesProfileAndMessage() = runTest {
        whenever(repository.getProfile()).thenReturn(
            Result.Success(UserProfile(1, "e@x.com", "Budi", "Bio"))
        )
        whenever(repository.updateProfile("Alicia", "New Bio")).thenReturn(
            Result.Success(UserProfile(1, "e@x.com", "Alicia", "New Bio"))
        )
        viewModel = ProfileViewModel(repository)
        advanceUntilIdle()
        viewModel.updateProfile("Alicia", "New Bio")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("Alicia", state.name)
        assertEquals("New Bio", state.bio)
        assertEquals("Profil diperbarui", state.message)
    }
}
