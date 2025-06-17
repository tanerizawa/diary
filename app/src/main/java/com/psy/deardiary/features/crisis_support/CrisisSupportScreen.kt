// LOKASI: app/src/main/java/com/psy/deardiary/features/crisis_support/CrisisSupportScreen.kt

package com.psy.deardiary.features.crisis_support

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.psy.deardiary.navigation.Screen
import com.psy.deardiary.ui.components.CrisisButton
import com.psy.deardiary.ui.components.SecondaryButton

@Composable
fun CrisisSupportScreen(
    navController: NavController, // PERBAIKAN: Tambah NavController
    viewModel: CrisisSupportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val emergencyContact by viewModel.emergencyContact.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Anda Tidak Sendirian", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Jika Anda merasa dalam krisis atau membutuhkan bantuan segera, jangan ragu untuk menghubungi layanan di bawah ini.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Tombol untuk kontak darurat pribadi
        val hasContact = !emergencyContact.isNullOrBlank()
        SecondaryButton(
            text = if (hasContact) "Hubungi Kontak Darurat" else "Atur Kontak Darurat",
            onClick = {
                if (hasContact) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$emergencyContact")
                    }
                    context.startActivity(intent)
                } else {
                    navController.navigate(Screen.EmergencyContactSettings.route)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol untuk bantuan nasional
        CrisisButton(
            text = "Hubungi Bantuan Nasional (119)",
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:119") }
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Aplikasi ini adalah pendamping, bukan pengganti bantuan profesional.",
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}
