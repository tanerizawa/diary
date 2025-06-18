package com.psy.deardiary.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psy.deardiary.data.model.ChatMessage
import com.psy.deardiary.data.repository.ChatRepository
import com.psy.deardiary.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class HomeChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    init {
        _messages.value = chatRepository.getConversation()
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            // 1. Tambahkan pesan pengguna ke history
            chatRepository.addMessage(text, isUser = true)

            // 2. Sisipkan pesan sementara sebagai indikator mengetik
            val placeholder = chatRepository.addMessage(
                "Sedang mengetik jawaban...",
                isUser = false,
                isPlaceholder = true
            )

            // Perbarui UI segera agar placeholder terlihat
            _messages.value = chatRepository.getConversation()

            // 3. Panggil API dengan batas waktu sepuluh detik
            val result = withTimeoutOrNull(10_000) { chatRepository.fetchReply(text) }

            // 4. Ganti pesan placeholder dengan hasil atau pesan kesalahan
            when (result) {
                is Result.Success -> {
                    chatRepository.replaceMessage(placeholder.id, result.data)
                }
                is Result.Error, null -> {
                    chatRepository.replaceMessage(
                        placeholder.id,
                        "Terjadi kesalahan."
                    )
                }
            }
            _messages.value = chatRepository.getConversation()
        }
    }
}
