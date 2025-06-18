package com.psy.deardiary.data.repository

import com.psy.deardiary.data.dto.UserProfileResponse
import com.psy.deardiary.data.dto.UserProfileUpdateRequest
import com.psy.deardiary.data.dto.toUserProfile
import com.psy.deardiary.data.model.UserProfile
import com.psy.deardiary.data.network.UserApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApiService: UserApiService
) {
    suspend fun getProfile(): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val response = userApiService.getProfile()
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!.toUserProfile())
                } else {
                    Result.Error("${'$'}{response.message()}")
                }
            } catch (e: HttpException) {
                Result.Error("Server error: ${'$'}{e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server.")
            }
        }
    }

    suspend fun updateProfile(name: String?, bio: String?): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            try {
                val body = UserProfileUpdateRequest(name, bio)
                val response = userApiService.updateProfile(body)
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!.toUserProfile())
                } else {
                    Result.Error("${'$'}{response.message()}")
                }
            } catch (e: HttpException) {
                Result.Error("Server error: ${'$'}{e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server.")
            }
        }
    }
}
