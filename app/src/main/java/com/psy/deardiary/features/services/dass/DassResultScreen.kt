// File BARU: app/src/main/java/com/psy/deardiary/features/services/dass/DassResultScreen.kt

package com.psy.deardiary.features.services.dass

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DassResultScreen(
    depressionScore: Int,
    anxietyScore: Int,
    stressScore: Int,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil Tes Anda") },
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
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Skor Anda (semakin tinggi, semakin berat gejalanya):",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            ResultItem("Depresi", depressionScore, getSeverity(DassScale.DEPRESSION, depressionScore))
            ResultItem("Kecemasan", anxietyScore, getSeverity(DassScale.ANXIETY, anxietyScore))
            ResultItem("Stres", stressScore, getSeverity(DassScale.STRESS, stressScore))
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Catatan: Hasil ini bukan diagnosis medis. Hubungi profesional jika Anda merasa khawatir.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ResultItem(label: String, score: Int, severity: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.titleLarge)
                Text(severity, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text((score * 2).toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        }
    }
}