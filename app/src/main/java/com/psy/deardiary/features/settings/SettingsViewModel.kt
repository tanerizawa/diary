// Lokasi: app/src/main/java/com/psy/deardiary/features/settings/SettingsViewModel.kt
// Deskripsi: Memperbarui logika hapus akun untuk memanggil repository.

package com.psy.deardiary.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.psy.deardiary.data.repository.AuthRepository
import com.psy.deardiary.data.repository.JournalRepository
import com.psy.deardiary.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val jsonForExport: String? = null,
    val userMessage: String? = null,
    val isAccountDeleted: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    // PERBAIKAN: Menambahkan kembali fungsi-fungsi yang hilang
    fun onExportDataClicked() {
        viewModelScope.launch {
            try {
                val entries = journalRepository.getAllEntriesOnce()
                if (entries.isEmpty()) {
                    _uiState.update { it.copy(userMessage = "Tidak ada data untuk diekspor.") }
                    return@launch
                }

                val gson = GsonBuilder().setPrettyPrinting().create()
                val json = gson.toJson(entries)

                _uiState.update { it.copy(jsonForExport = json) }

            } catch (e: Exception) {
                _uiState.update { it.copy(userMessage = "Terjadi kesalahan saat menyiapkan data.") }
            }
        }
    }

    fun onExportComplete() {
        _uiState.update { it.copy(jsonForExport = null, userMessage = "Data berhasil diekspor!") }
    }

    fun onUserMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            when (authRepository.deleteAccountOnServer()) {
                is Result.Success -> {
                    journalRepository.deleteAllLocalEntries()
                    authRepository.logout()
                    _uiState.update { it.copy(isAccountDeleted = true) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(userMessage = "Gagal menghapus akun. Coba lagi.") }
                }
            }
        }
    }
}