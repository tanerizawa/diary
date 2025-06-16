// File: app/src/main/java/com/psy/deardiary/features/services/ServicesScreen.kt
// VERSI DIBERSIHKAN: Impor tidak duplikat dan struktur Compose diperjelas

package com.psy.deardiary.features.services.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.psy.deardiary.navigation.Screen
import com.psy.deardiary.ui.components.CrisisButton
import com.psy.deardiary.ui.theme.DearDiaryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    navController: NavController,
    viewModel: ServicesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Layanan & Asesmen") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Bagian: Tes Psikologi Mandiri ---
                item {
                    Text(
                        text = "Tes Psikologi Mandiri",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(uiState.availableTests) { serviceItem ->
                    ServiceCard(service = serviceItem, onClick = {
                        when {
                            serviceItem.title.contains("MBTI", ignoreCase = true) -> {
                                navController.navigate(Screen.MbtiTest.route)
                            }
                            serviceItem.title.contains("Stres", ignoreCase = true) -> {
                                navController.navigate(Screen.DassTest.route)
                            }
                        }
                    })
                }

                // --- Bagian: Bantuan Profesional ---
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Bantuan Profesional",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(uiState.professionalServices) { serviceItem ->
                    ServiceCard(service = serviceItem, onClick = {
                        // TODO: Navigasi ke direktori layanan profesional
                    })
                }
            }

            // --- Tombol Darurat ---
            CrisisButton(
                text = "Butuh Bantuan Darurat? (119)",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:119")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun ServiceCard(service: ServiceItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = service.color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = service.icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = service.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServicesScreenPreview() {
    DearDiaryTheme {
        // Preview dummy hanya menampilkan teks karena NavController tidak tersedia
        Text("Preview tidak tersedia karena membutuhkan NavController")
    }
}
