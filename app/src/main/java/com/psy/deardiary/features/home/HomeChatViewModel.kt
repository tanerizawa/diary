package com.psy.deardiary.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.psy.deardiary.data.model.ChatMessage
import com.psy.deardiary.data.repository.ChatRepository
import com.psy.deardiary.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class HomeChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val PROMPT_COOLDOWN = 6 * 60 * 60 * 1000L
    private val AUTO_PROMPT_AFTER = 12 * 60 * 60 * 1000L

    data class UiState(
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _pendingAction = MutableStateFlow<String?>(null)
    val pendingAction = _pendingAction.asStateFlow()
    val journalTemplate = chatRepository.journalTemplate

    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedIds = _selectedIds.asStateFlow()

    val latestSentiment = chatRepository.latestSentiment

    init {
        viewModelScope.launch {
            when (val result = chatRepository.refreshMessages()) {
                is Result.Error -> Log.e(
                    "HomeChatViewModel",
                    "refreshMessages failed: ${result.message}"
                )
                else -> Unit
            }
            checkAutoPrompt()
            chatRepository.getConversation().collect { history ->
                _messages.value = history
            }
        }
    }

    private suspend fun checkAutoPrompt() {
        val lastPrompt = chatRepository.userPreferencesRepository.lastAiPrompt.first() ?: 0L
        val now = System.currentTimeMillis()
        if (now - lastPrompt < PROMPT_COOLDOWN) return

        val history = chatRepository.messages.first()
        val lastActivity = history.lastOrNull()?.timestamp ?: 0L
        if (now - lastActivity > AUTO_PROMPT_AFTER) {
            when (chatRepository.promptChat()) {
                is Result.Error -> Log.e("HomeChatViewModel", "promptChat failed")
                else -> Unit
            }
        }
    }

    fun toggleSelection(id: Int) {
        _selectedIds.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelectedMessages() {
        viewModelScope.launch {
            val ids = _selectedIds.value.toList()
            if (ids.isEmpty()) return@launch
            when (val result = chatRepository.deleteMessages(ids)) {
                is Result.Success -> _selectedIds.value = emptySet()
                is Result.Error -> _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            // 1. Tambahkan pesan pengguna ke history
            val userMsg = chatRepository.addMessage(text, isUser = true)

            // 2. Sisipkan pesan sementara sebagai indikator mengetik
            val placeholder = chatRepository.addMessage(
                "Sedang mengetik jawaban...",
                isUser = false,
                isPlaceholder = true
            )

            // 3. Tunda pengiriman pesan sekitar 5 detik untuk meniru jeda ketika
            //    manusia mengetik. Setelah itu baru teruskan ke server.
            delay(5_000)

            // 4. Panggil API dengan batas waktu lebih lama agar server punya waktu
            //    yang cukup untuk merespons. Batas lama sebelumnya kadang terlalu
            //    singkat sehingga balasan AI tidak sempat diterima sepenuhnya.
            val result = withTimeoutOrNull(30_000) {
                chatRepository.sendMessage(text, userMsg.id)
            }

            // 5. Ganti pesan placeholder dengan hasil atau pesan kesalahan
            when (result) {
                is Result.Success -> {
                    val response = result.data
                    chatRepository.updateMessageWithReply(
                        id = placeholder.id,
                        replyText = response.textResponse,
                        detectedMood = response.detectedMood,
                        remoteId = response.replyId
                    )
                    if (response.action != "balas_teks") {
                        _pendingAction.value = response.action
                    }
                }
                is Result.Error, null -> {
                    chatRepository.replaceMessage(
                        placeholder.id,
                        "Terjadi kesalahan."
                    )
                }
            }

            // 6. Sinkronkan pesan yang belum terkirim dan tangani error
            when (val syncResult = chatRepository.syncPendingMessages()) {
                is Result.Error -> _uiState.update { it.copy(errorMessage = syncResult.message) }
                else -> Unit
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeAction() {
        _pendingAction.value = null
        chatRepository.clearJournalTemplate()
    }
}
