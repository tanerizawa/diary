// File: app/src/main/java/com/psy/deardiary/features/auth/RegisterScreen.kt
// Deskripsi: Layar untuk pengguna baru mendaftarkan akun.
// VERSI DIPERBARUI: Ditambahkan validasi konfirmasi password.

package com.psy.deardiary.features.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psy.deardiary.ui.components.PasswordTextField
import com.psy.deardiary.ui.components.PrimaryButton
import com.psy.deardiary.ui.components.PrimaryTextField
import com.psy.deardiary.ui.theme.DearDiaryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean // BARU: State untuk mengetahui proses loading
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    // BARU: State untuk menyimpan pesan error validasi
    var passwordError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Buat Akun Baru",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Mulai perjalananmu bersama kami",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))
            PrimaryTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordTextField(
                value = password,
                onValueChange = { password = it; passwordError = null }, // Hapus error saat diketik
                label = "Password",
                isError = passwordError != null // Tandai error jika ada
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; passwordError = null }, // Hapus error saat diketik
                label = "Konfirmasi Password",
                isError = passwordError != null, // Tandai error jika ada
                errorMessage = passwordError // Tampilkan pesan error di sini
            )
            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = "Daftar",
                onClick = {
                    // MODIFIKASI: Tambahkan logika validasi di sini
                    if (password != confirmPassword) {
                        passwordError = "Konfirmasi password tidak cocok."
                    } else if (password.length < 6) { // Contoh validasi lain
                        passwordError = "Password minimal harus 6 karakter."
                    }
                    else {
                        passwordError = null
                        onRegisterClick(email, password)
                    }
                },
                // BARU: Nonaktifkan tombol saat sedang loading
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = buildAnnotatedString {
                    append("Sudah punya akun? ")
                    withStyle(style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )) {
                        append("Login")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    DearDiaryTheme {
        RegisterScreen(
            onRegisterClick = { _, _ -> },
            onNavigateToLogin = {},
            onBackClick = {},
            isLoading = false
        )
    }
}