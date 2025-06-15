import com.psy.deardiary.data.repository.AuthRepository
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.model.JournalEntry
import com.psy.deardiary.features.settings.SettingsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var journalRepository: JournalRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        authRepository = mockk()
        journalRepository = mockk()
        viewModel = SettingsViewModel(authRepository, journalRepository, mockk())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun exportData_emitsJson_whenEntriesExist() = runTest(dispatcher) {
        val entries = listOf(JournalEntry(title="t", content="c", mood="😊", tags=emptyList()))
        coEvery { journalRepository.getAllEntriesOnce() } returns entries

        viewModel.onExportDataClicked()
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.jsonForExport)
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun exportData_showsMessage_whenNoEntries() = runTest(dispatcher) {
        coEvery { journalRepository.getAllEntriesOnce() } returns emptyList()

        viewModel.onExportDataClicked()
        advanceUntilIdle()

        assertEquals("Tidak ada data untuk diekspor.", viewModel.uiState.value.userMessage)
    }

    @Test
    fun exportComplete_clearsJson_andSetsMessage() = runTest(dispatcher) {
        coEvery { journalRepository.getAllEntriesOnce() } returns listOf(JournalEntry(title="t", content="c", mood="😊", tags=emptyList()))
        viewModel.onExportDataClicked()
        advanceUntilIdle()

        viewModel.onExportComplete()

        assertNull(viewModel.uiState.value.jsonForExport)
        assertEquals("Data berhasil diekspor!", viewModel.uiState.value.userMessage)
    }

    @Test
    fun userMessageShown_clearsMessage() = runTest(dispatcher) {
        coEvery { journalRepository.getAllEntriesOnce() } returns emptyList()
        viewModel.onExportDataClicked()
        advanceUntilIdle()

        viewModel.onUserMessageShown()
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun deleteAccount_success_updatesState() = runTest(dispatcher) {
        coEvery { authRepository.deleteAccountOnServer() } returns Result.Success(Unit)
        coEvery { journalRepository.deleteAllLocalEntries() } returns Unit
        coEvery { authRepository.logout() } returns Unit

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isAccountDeleted)
        coVerify { journalRepository.deleteAllLocalEntries() }
        coVerify { authRepository.logout() }
    }

    @Test
    fun deleteAccount_error_showsMessage() = runTest(dispatcher) {
        coEvery { authRepository.deleteAccountOnServer() } returns Result.Error("error")

        viewModel.deleteAccount()
        advanceUntilIdle()

        assertEquals("Gagal menghapus akun. Coba lagi.", viewModel.uiState.value.userMessage)
    }
}
