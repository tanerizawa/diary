// File Baru: app/src/main/java/com/psy/deardiary/features/services/mbti/MbtiTestViewModel.kt
// Deskripsi: ViewModel untuk mengelola state dan logika tes MBTI.

package com.psy.deardiary.features.services.mbti.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.psy.deardiary.features.services.mbti.data.MbtiQuestion
import com.psy.deardiary.features.services.mbti.data.MbtiDimension
import com.psy.deardiary.features.services.mbti.data.mbtiQuestions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MbtiTestUiState(
    val questions: List<MbtiQuestion> = mbtiQuestions,
    val currentQuestionIndex: Int = 0,
    val progress: Float = 0f,
    val isFinished: Boolean = false,
    val resultType: String? = null
)

@HiltViewModel
class MbtiTestViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MbtiTestUiState())
    val uiState = _uiState.asStateFlow()

    // Map untuk menyimpan skor untuk setiap dimensi
    private val scores = mutableMapOf(
        MbtiDimension.INTROVERSION_EXTRAVERSION to 0,
        MbtiDimension.SENSING_INTUITION to 0,
        MbtiDimension.THINKING_FEELING to 0,
        MbtiDimension.JUDGING_PERCEIVING to 0
    )

    fun answerQuestion(isYes: Boolean) {
        val currentState = _uiState.value
        val question = currentState.questions[currentState.currentQuestionIndex]

        // Logika skoring
        if (isYes == question.scoresForIntrovert) {
            scores[question.dimension] = scores.getOrDefault(question.dimension, 0) + 1
        } else {
            scores[question.dimension] = scores.getOrDefault(question.dimension, 0) - 1
        }

        // Pindah ke pertanyaan berikutnya atau selesaikan tes
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            val nextIndex = currentState.currentQuestionIndex + 1
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    progress = (nextIndex.toFloat() / currentState.questions.size)
                )
            }
        } else {
            calculateResult()
        }
    }

    private fun calculateResult() {
        val result = StringBuilder()
        result.append(if (scores.getOrDefault(MbtiDimension.INTROVERSION_EXTRAVERSION, 0) > 0) "I" else "E")
        result.append(if (scores.getOrDefault(MbtiDimension.SENSING_INTUITION, 0) > 0) "S" else "N")
        result.append(if (scores.getOrDefault(MbtiDimension.THINKING_FEELING, 0) > 0) "T" else "F")
        result.append(if (scores.getOrDefault(MbtiDimension.JUDGING_PERCEIVING, 0) > 0) "J" else "P")

        _uiState.update {
            it.copy(
                isFinished = true,
                resultType = result.toString(),
                progress = 1f
            )
        }
    }
}
