package com.psy.deardiary.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.psy.deardiary.data.repository.AuthRepository
import com.psy.deardiary.data.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val journalRepository: JournalRepository
) : ViewModel() {

    fun exportData() {
        viewModelScope.launch {
            val entries = journalRepository.getAllEntriesOnce()
            val json = Gson().toJson(entries)
            println("Exported Data: $json")
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            journalRepository.deleteAllLocalEntries()
            authRepository.logout()
        }
    }
}
