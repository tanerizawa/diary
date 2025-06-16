// File BARU: app/src/main/java/com/psy/deardiary/features/services/dass/DassTestViewModel.kt

package com.psy.deardiary.features.services.dass

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class DassTestUiState(
    val questions: List<DassQuestion> = dassQuestions,
    val currentQuestionIndex: Int = 0,
    val progress: Float = 0f,
    val isFinished: Boolean = false,
    val finalScores: Map<DassScale, Int> = emptyMap()
)

@HiltViewModel
class DassTestViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DassTestUiState())
    val uiState = _uiState.asStateFlow()

    private val scores = mutableMapOf(
        DassScale.DEPRESSION to 0,
        DassScale.ANXIETY to 0,
        DassScale.STRESS to 0
    )

    fun answerQuestion(score: Int) {
        val currentState = _uiState.value
        val question = currentState.questions[currentState.currentQuestionIndex]

        scores[question.scale] = scores.getOrDefault(question.scale, 0) + score

        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            val nextIndex = currentState.currentQuestionIndex + 1
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    progress = (nextIndex.toFloat() / currentState.questions.size)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isFinished = true,
                    finalScores = scores,
                    progress = 1f
                )
            }
        }
    }
}