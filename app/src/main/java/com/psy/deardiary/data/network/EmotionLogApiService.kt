package com.psy.deardiary.data.network

import com.psy.deardiary.data.dto.EmotionLogResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface EmotionLogApiService {
    @GET("api/v1/emotion")
    suspend fun getEmotionLogs(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<List<EmotionLogResponse>>
}
