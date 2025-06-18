package com.psy.deardiary.data.network

import com.psy.deardiary.data.dto.FeedItemResponse
import retrofit2.Response
import retrofit2.http.GET

interface FeedApiService {
    @GET("api/v1/feed")
    suspend fun getFeed(): Response<List<FeedItemResponse>>
}
