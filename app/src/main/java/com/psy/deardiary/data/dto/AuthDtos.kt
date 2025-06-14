// File Baru: app/src/main/java/com/psy/deardiary/data/dto/AuthDtos.kt
// Deskripsi: Data Transfer Objects (DTOs). Ini adalah data class sederhana yang
// strukturnya cocok persis dengan JSON yang dikirim dan diterima dari backend FastAPI kita.

package com.psy.deardiary.data.dto

import com.google.gson.annotations.SerializedName

// Data yang dikirim ke server saat login (cocok dengan OAuth2PasswordRequestForm di FastAPI)
// FastAPI akan membaca 'username' dan 'password' dari form data.
// Kita akan membuatnya secara manual saat memanggil API.

// Respons yang diterima dari server setelah login berhasil
data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String
)

// Respons umum untuk error dari server
data class ErrorResponse(
    @SerializedName("detail")
    val detail: String
)