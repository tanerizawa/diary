// File: app/src/main/java/com/psy/deardiary/features/settings/NotificationSettingsScreen.kt
// VERSI DIPERBARUI: Menghubungkan UI dengan logika notifikasi.

package com.psy.deardiary.features.settings

import android.Manifest
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.psy.deardiary.utils.NotificationReceiver
import com.psy.deardiary.ui.components.PermissionDeniedDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    // Untuk saat ini, kita akan kelola state secara lokal.
    // Untuk aplikasi nyata, ini sebaiknya disimpan di DataStore.
    var remindersEnabled by remember { mutableStateOf(false) }

    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                remindersEnabled = true
                NotificationReceiver.scheduleDailyReminder(context)
            } else {
                showPermissionDialog = true
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Notifikasi") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ingatkan menulis jurnal (20.00)",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            // Cek dan minta izin jika diperlukan
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                remindersEnabled = true
                                NotificationReceiver.scheduleDailyReminder(context)
                            }
                        } else {
                            remindersEnabled = false
                            NotificationReceiver.cancelDailyReminder(context)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Anda akan menerima pengingat setiap hari pada jam 8 malam.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showPermissionDialog) {
            PermissionDeniedDialog(
                onDismissRequest = { showPermissionDialog = false },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                message = "Izin notifikasi diperlukan agar pengingat dapat ditampilkan."
            )
        }
    }
}
