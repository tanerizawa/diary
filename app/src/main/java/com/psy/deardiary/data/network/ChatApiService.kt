package com.psy.deardiary.data.network

import com.psy.deardiary.data.dto.ChatRequest
import com.psy.deardiary.data.dto.ChatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {
    @POST("api/v1/chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>
}
