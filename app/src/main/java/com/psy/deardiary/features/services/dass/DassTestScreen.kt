// File BARU: app/src/main/java/com/psy/deardiary/features/services/dass/DassTestScreen.kt

package com.psy.deardiary.features.services.dass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DassTestScreen(
    onNavigateBack: () -> Unit,
    onTestComplete: (depression: Int, anxiety: Int, stress: Int) -> Unit,
    viewModel: DassTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val animatedProgress by animateFloatAsState(targetValue = uiState.progress, label = "progress")

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            val scores = uiState.finalScores
            onTestComplete(
                scores[DassScale.DEPRESSION] ?: 0,
                scores[DassScale.ANXIETY] ?: 0,
                scores[DassScale.STRESS] ?: 0
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tes Stres, Cemas, & Depresi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pertanyaan ${uiState.currentQuestionIndex + 1} dari ${uiState.questions.size}",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = uiState.questions[uiState.currentQuestionIndex].text,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                dassAnswerOptions.forEach { (text, score) ->
                    Button(
                        onClick = { viewModel.answerQuestion(score) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text)
                    }
                }
            }
        }
    }
}