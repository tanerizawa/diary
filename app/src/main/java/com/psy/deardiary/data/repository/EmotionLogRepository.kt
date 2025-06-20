package com.psy.deardiary.data.repository

import com.psy.deardiary.data.dto.toEmotionLog
import com.psy.deardiary.data.network.EmotionLogApiService
import com.psy.deardiary.data.model.EmotionLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmotionLogRepository @Inject constructor(
    private val emotionLogApiService: EmotionLogApiService
) {
    private val _logs = MutableStateFlow<List<EmotionLog>>(emptyList())
    val logs = _logs.asStateFlow()

    suspend fun refreshEmotionLogs(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = emotionLogApiService.getEmotionLogs()
                if (response.isSuccessful && response.body() != null) {
                    _logs.value = response.body()!!.map { it.toEmotionLog() }
                    Result.Success(Unit)
                } else {
                    Result.Error(response.message())
                }
            } catch (e: HttpException) {
                Result.Error("Server error: ${e.code()}")
            } catch (e: IOException) {
                Result.Error("Tidak dapat terhubung ke server.")
            }
        }
    }
}
