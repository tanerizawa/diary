// File: app/src/main/java/com/psy/deardiary/features/crisis_support/CrisisSupportScreen.kt
// Deskripsi: Layar yang menampilkan informasi darurat dan kontak bantuan
// profesional, sesuai dengan prinsip keamanan aplikasi.

package com.psy.deardiary.features.crisis_support

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.psy.deardiary.ui.components.CrisisButton

@Composable
fun CrisisSupportScreen() {
    // Dapatkan konteks saat ini untuk memulai Intent
    val context = LocalContext.current

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

        // Menggunakan komponen CrisisButton yang baru
        CrisisButton(
            text = "Hubungi Bantuan Sekarang (119)",
            onClick = {
                // Membuat Intent untuk membuka dialer dengan nomor 119
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:119")
                }
                // Memulai aktivitas (membuka dialer)
                context.startActivity(intent)
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aplikasi ini adalah pendamping, bukan pengganti bantuan profesional.",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
    }
}
