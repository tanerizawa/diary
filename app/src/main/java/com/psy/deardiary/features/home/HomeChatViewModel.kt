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
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class HomeChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    val latestSentiment = chatRepository.latestSentiment

    init {
        viewModelScope.launch {
            chatRepository.getConversation().collect { history ->
                _messages.value = history
            }
        }
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

            // 3. Tunda pengiriman pesan sekitar 5 detik untuk meniru jeda ketika
            //    manusia mengetik. Setelah itu baru teruskan ke server.
            delay(5_000)

            // 4. Panggil API dengan batas waktu lebih lama agar server punya waktu
            //    yang cukup untuk merespons. Batas lama sebelumnya kadang terlalu
            //    singkat sehingga balasan AI tidak sempat diterima sepenuhnya.
            val result = withTimeoutOrNull(30_000) { chatRepository.fetchReply(text) }

            // 5. Ganti pesan placeholder dengan hasil atau pesan kesalahan
            when (result) {
                is Result.Success -> {
                    val response = result.data
                    chatRepository.replaceMessage(
                        placeholder.id,
                        response.replyText,
                        detectedMood = response.detectedMood
                    )
                }
                is Result.Error, null -> {
                    chatRepository.replaceMessage(
                        placeholder.id,
                        "Terjadi kesalahan."
                    )
                }
            }
        }
    }
}
