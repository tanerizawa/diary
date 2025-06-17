// File: app/src/main/java/com/psy/deardiary/ui/components/AppStates.kt
// Deskripsi: Berisi komponen-komponen UI untuk merepresentasikan berbagai
// status aplikasi, seperti error jaringan.

package com.psy.deardiary.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.psy.deardiary.ui.theme.Error

@Composable
fun NetworkErrorSnackbar(
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    message: String = "Gagal terhubung. Periksa koneksi internet Anda."
) {
    SnackbarHost(hostState = snackbarHostState, modifier = modifier) { data ->
        Snackbar(
            containerColor = Error,
            contentColor = Color.White,
            action = {
                TextButton(onClick = onRetry) {
                    Text("Coba Lagi", color = Color.White)
                }
            },
            dismissAction = {
                // Bisa ditambahkan jika ingin ada tombol close
            }
        ) {
            Text(message)
        }
    }
}
