package com.psy.deardiary.data.network

import com.psy.deardiary.data.dto.UserProfileResponse
import com.psy.deardiary.data.dto.UserProfileUpdateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface UserApiService {
    @GET("api/v1/users/me")
    suspend fun getProfile(): Response<UserProfileResponse>

    @PUT("api/v1/users/me")
    suspend fun updateProfile(@Body body: UserProfileUpdateRequest): Response<UserProfileResponse>
}
