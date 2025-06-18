// Lokasi: app/src/main/java/com/psy/deardiary/data/repository/AuthRepository.kt
// Deskripsi: Menambahkan fungsi untuk memanggil API penghapusan akun.

package com.psy.deardiary.data.repository

import com.psy.deardiary.data.datastore.UserPreferencesRepository
import android.util.Base64
import org.json.JSONObject
import com.psy.deardiary.data.dto.LoginRequest
import com.psy.deardiary.data.dto.RegisterRequest
import com.psy.deardiary.data.network.AuthApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun login(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.login(LoginRequest(email, password))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.accessToken
                    userPreferencesRepository.saveAuthToken(token)
                    parseUserIdFromToken(token)?.let { userPreferencesRepository.saveUserId(it) }
                    Result.Success(Unit)
                } else {
                    Result.Error("Login gagal: ${response.message()}")
                }
            } catch (e: HttpException) {
                Result.Error("Terjadi kesalahan pada server. Kode: ${e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
            }
        }
    }

    // PERBAIKAN: Melengkapi fungsi register yang hilang
    suspend fun register(email: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.register(RegisterRequest(email, password))
                if (response.isSuccessful) {
                    Result.Success(Unit)
                } else {
                    Result.Error("Registrasi gagal: ${response.message()}")
                }
            } catch (e: HttpException) {
                Result.Error("Terjadi kesalahan pada server. Kode: ${e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda.")
            }
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            userPreferencesRepository.clearAuthToken()
            userPreferencesRepository.clearUserId()
        }
    }

    suspend fun deleteAccountOnServer(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = authApiService.deleteAccount()
                if (response.isSuccessful) {
                    Result.Success(Unit)
                } else {
                    Result.Error("Gagal menghapus akun di server: ${response.message()}")
                }
            } catch (e: HttpException) {
                Result.Error("Terjadi kesalahan pada server. Kode: ${e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server.")
            }
        }
    }

    private fun parseUserIdFromToken(token: String): Int? {
        return try {
            val payload = token.split(".")[1]
            val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val json = JSONObject(String(decoded, Charsets.UTF_8))
            json.getString("sub").toIntOrNull()
        } catch (e: Exception) {
            null
        }
    }
}
