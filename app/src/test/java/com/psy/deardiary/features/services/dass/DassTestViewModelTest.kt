import com.psy.deardiary.features.services.dass.DassTestViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DassTestViewModelTest {
    @Test
    fun completeTest_collectsScores() {
        val vm = DassTestViewModel()
        vm.uiState.value.questions.forEach { _ ->
            vm.answerQuestion(1)
        }
        assertTrue(vm.uiState.value.isFinished)
        assertEquals(1f, vm.uiState.value.progress)
        assertTrue(vm.uiState.value.finalScores.isNotEmpty())
    }
}
