// File: app/src/main/java/com/psy/deardiary/features/settings/SettingsScreen.kt
// Deskripsi: Layar untuk semua pengaturan aplikasi, memberikan pengguna kontrol
// penuh atas data dan preferensi mereka.

package com.psy.deardiary.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psy.deardiary.ui.components.ConfirmationDialog
import com.psy.deardiary.ui.theme.DearDiaryTheme
import com.psy.deardiary.ui.theme.Error
import androidx.compose.ui.text.font.FontWeight // <-- TAMBAHKAN BARIS INI


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onExportData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                onDeleteAccount()
                showDeleteDialog = false
            },
            title = "Hapus Akun",
            text = "Anda yakin ingin menghapus akun dan semua data jurnal Anda? Aksi ini tidak dapat dibatalkan.",
            confirmText = "Ya, Hapus"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Grup Data & Privasi
            Text("Data & Privasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SettingItem(
                icon = Icons.Default.Download,
                title = "Ekspor Semua Data",
                description = "Simpan semua entri jurnalmu sebagai file.",
                onClick = onExportData
            )
            SettingItem(
                icon = Icons.Default.DeleteForever,
                title = "Hapus Akun & Semua Data",
                description = "Aksi ini bersifat permanen.",
                onClick = { showDeleteDialog = true },
                isDestructive = true
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Grup Aplikasi
            Text("Aplikasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SettingItem(
                icon = Icons.Default.Notifications,
                title = "Pengaturan Notifikasi",
                description = "Atur pengingat untuk menulis jurnal.",
                onClick = onNavigateToNotification
            )
            SettingItem(
                icon = Icons.Default.Policy,
                title = "Kebijakan Privasi",
                description = "Baca bagaimana kami melindungi datamu.",
                onClick = onNavigateToPrivacyPolicy
            )
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) Error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) Error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    DearDiaryTheme {
        SettingsScreen({}, {}, {}, {}, {})
    }
}
