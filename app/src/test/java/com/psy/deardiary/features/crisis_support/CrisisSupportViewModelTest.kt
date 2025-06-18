import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.features.crisis_support.CrisisSupportViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class CrisisSupportViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var prefRepo: UserPreferencesRepository
    private lateinit var viewModel: CrisisSupportViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        prefRepo = mock()
        whenever(prefRepo.emergencyContact).thenReturn(MutableStateFlow("999"))
        viewModel = CrisisSupportViewModel(prefRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun exposesEmergencyContactFlow() = runTest {
        assertEquals("999", viewModel.emergencyContact.value)
    }
}
