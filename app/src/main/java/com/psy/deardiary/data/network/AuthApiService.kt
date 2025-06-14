// File Baru: app/src/main/java/com/psy/deardiary/data/network/AuthApiService.kt
// Deskripsi: Interface Retrofit. Di sinilah kita mendefinisikan semua endpoint
// API yang berhubungan dengan autentikasi.

package com.psy.deardiary.data.network

import com.psy.deardiary.data.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface AuthApiService {

    // Endpoint untuk registrasi pengguna baru
    @FormUrlEncoded
    @POST("api/v1/users/register")
    suspend fun register(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<Unit> // Kita tidak mengharapkan body respons saat sukses

    // Endpoint untuk login. @FormUrlEncoded karena FastAPI mengharapkan form data
    @FormUrlEncoded
    @POST("api/v1/users/login")
    suspend fun login(
        @Field("username") email: String, // FastAPI OAuth2 form menggunakan 'username'
        @Field("password") password: String
    ): Response<TokenResponse>
}