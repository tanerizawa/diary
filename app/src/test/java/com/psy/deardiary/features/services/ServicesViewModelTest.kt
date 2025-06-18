import com.psy.deardiary.features.services.ServicesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ServicesViewModelTest {
    @Test
    fun initialState_containsServiceItems() {
        val vm = ServicesViewModel()
        assertTrue(vm.uiState.value.availableTests.isNotEmpty())
        assertTrue(vm.uiState.value.professionalServices.isNotEmpty())
    }
}
