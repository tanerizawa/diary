// File: app/src/main/java/com/psy/deardiary/features/crisis_support/CrisisSupportScreen.kt
// Deskripsi: Layar yang menampilkan informasi darurat dan kontak bantuan
// profesional, sesuai dengan prinsip keamanan aplikasi.

package com.psy.deardiary.features.crisis_support

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CrisisSupportScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Anda Tidak Sendirian",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Jika Anda merasa dalam krisis atau membutuhkan bantuan segera, jangan ragu untuk menghubungi layanan kesehatan mental profesional.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { /* TODO: Arahkan ke nomor darurat atau kontak yang relevan */ }) {
            Text("Hubungi Bantuan Sekarang")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aplikasi ini adalah pendamping, bukan pengganti bantuan profesional.",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}