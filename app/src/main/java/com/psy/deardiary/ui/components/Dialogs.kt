// File: app/src/main/java/com/psy/deardiary/ui/components/Dialogs.kt
// Deskripsi: Komponen dialog untuk konfirmasi aksi penting.

package com.psy.deardiary.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.psy.deardiary.ui.theme.Error

@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String,
    confirmText: String = "Ya",
    dismissText: String = "Batal"
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismissRequest()
                }
            ) {
                Text(confirmText, color = Error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(dismissText)
            }
        }
    )
}

@Composable
fun InfoDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmText: String = "OK",
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(confirmText)
            }
        },
    )
}

@Composable
fun PermissionDeniedDialog(
    onDismissRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    message: String,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Izin Diperlukan") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = { onOpenSettings(); onDismissRequest() }) {
                Text("Buka Pengaturan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Tutup") }
        }
    )
}
