// Lokasi: app/src/main/java/com/psy/deardiary/features/settings/SettingsScreen.kt
// Deskripsi: UI pengaturan lengkap dengan ekspor data, hapus akun, dan navigasi fitur tambahan.

package com.psy.deardiary.features.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactEmergency
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.deardiary.ui.components.ConfirmationDialog
import com.psy.deardiary.ui.theme.DearDiaryTheme
import com.psy.deardiary.ui.theme.Error
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToEmergencyContact: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val fileCreatorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let { fileUri ->
                uiState.jsonForExport?.let { json ->
                    try {
                        context.contentResolver.openOutputStream(fileUri)?.use { it.write(json.toByteArray()) }
                        viewModel.onExportComplete()
                    } catch (e: Exception) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Gagal menyimpan file.")
                        }
                    }
                }
            }
        }
    )

    LaunchedEffect(uiState) {
        uiState.jsonForExport?.let {
            fileCreatorLauncher.launch("deardiary_export_${System.currentTimeMillis()}.json")
        }

        uiState.userMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.onUserMessageShown()
            }
        }
    }

    LaunchedEffect(uiState.isAccountDeleted) {
        if (uiState.isAccountDeleted) {
            onAccountDeleted()
        }
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteAccount()
                showDeleteDialog = false
            },
            title = "Hapus Akun",
            text = "Anda yakin ingin menghapus akun dan semua data jurnal Anda? Aksi ini tidak dapat dibatalkan.",
            confirmText = "Ya, Hapus"
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            Text("Data & Privasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SettingItem(
                icon = Icons.Default.Download,
                title = "Ekspor Semua Data",
                description = "Simpan semua entri jurnalmu sebagai file.",
                onClick = { viewModel.onExportDataClicked() }
            )
            SettingItem(
                icon = Icons.Default.DeleteForever,
                title = "Hapus Akun & Semua Data",
                description = "Aksi ini bersifat permanen.",
                onClick = { showDeleteDialog = true },
                isDestructive = true
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Akun", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SettingItem(
                icon = Icons.Default.Person,
                title = "Profil Saya",
                description = "Lihat dan ubah informasi akun.",
                onClick = onNavigateToProfile
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text("Aplikasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            SettingItem(
                icon = Icons.Default.ContactEmergency,
                title = "Kontak Darurat",
                description = "Atur nomor yang dihubungi saat krisis.",
                onClick = onNavigateToEmergencyContact
            )
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
        SettingsScreen(
            onBackClick = {},
            onNavigateToNotification = {},
            onNavigateToPrivacyPolicy = {},
            onNavigateToEmergencyContact = {},
            onNavigateToProfile = {},
            onAccountDeleted = {}
        )
    }
}
