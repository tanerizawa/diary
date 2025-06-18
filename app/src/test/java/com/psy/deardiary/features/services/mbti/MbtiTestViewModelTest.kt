import com.psy.deardiary.features.services.mbti.MbtiTestViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MbtiTestViewModelTest {
    @Test
    fun completeTest_producesResult() {
        val vm = MbtiTestViewModel()
        vm.uiState.value.questions.forEach { _ ->
            vm.answerQuestion(true)
        }
        assertTrue(vm.uiState.value.isFinished)
        assertEquals(1f, vm.uiState.value.progress)
        assertTrue(vm.uiState.value.resultType != null)
    }
}
