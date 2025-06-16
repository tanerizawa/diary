// File Baru: app/src/main/java/com/psy/deardiary/features/services/mbti/MbtiTestScreen.kt
// Deskripsi: UI untuk mengerjakan tes MBTI.

package com.psy.deardiary.features.services.mbti.presentation

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
import com.psy.deardiary.ui.components.PrimaryButton
import com.psy.deardiary.ui.components.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MbtiTestScreen(
    onNavigateBack: () -> Unit,
    onTestComplete: (String) -> Unit,
    viewModel: MbtiTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val animatedProgress by animateFloatAsState(targetValue = uiState.progress, label = "progressAnimation")

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            uiState.resultType?.let { onTestComplete(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tes Kepribadian MBTI") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth()
                )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SecondaryButton(
                    text = "Tidak",
                    onClick = { viewModel.answerQuestion(false) },
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "Ya",
                    onClick = { viewModel.answerQuestion(true) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
