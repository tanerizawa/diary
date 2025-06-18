import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.repository.AuthRepository
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.features.settings.SettingsViewModel
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepo: AuthRepository
    private lateinit var journalRepo: JournalRepository
    private lateinit var prefRepo: UserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        authRepo = mock()
        journalRepo = mock()
        prefRepo = mock()
        whenever(prefRepo.emergencyContact).thenReturn(MutableStateFlow(null))
        viewModel = SettingsViewModel(authRepo, journalRepo, prefRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveEmergencyContact_callsRepository() = runTest {
        viewModel.saveEmergencyContact("123")
        advanceUntilIdle()
        verify(prefRepo).saveEmergencyContact("123")
    }
}
