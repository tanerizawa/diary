// File: app/src/main/java/com/psy/deardiary/features/auth/RegisterScreen.kt
// Deskripsi: Layar untuk pengguna baru mendaftarkan akun.
// VERSI DIPERBARUI: Ditambahkan indikator loading dan penanganan error.

package com.psy.deardiary.features.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.deardiary.ui.components.PasswordTextField
import com.psy.deardiary.ui.components.PrimaryButton
import com.psy.deardiary.ui.components.PrimaryTextField
import com.psy.deardiary.ui.theme.DearDiaryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onBackClick: () -> Unit,
    // Menghapus parameter onRegisterClick dan isLoading
    authViewModel: AuthViewModel = hiltViewModel(),
    mainViewModel: com.psy.deardiary.features.main.MainViewModel
) {
    val uiState by authViewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            mainViewModel.showError(message)
            authViewModel.clearErrorMessage()
        }
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    label = "Email",
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null },
                    label = "Password",
                    isError = passwordError != null,
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(16.dp))
                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; passwordError = null },
                    label = "Konfirmasi Password",
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(32.dp))
                PrimaryButton(
                    text = "Daftar",
                    onClick = {
                        if (password != confirmPassword) {
                            passwordError = "Konfirmasi password tidak cocok."
                        } else if (password.length < 6) {
                            passwordError = "Password minimal harus 6 karakter."
                        } else {
                            passwordError = null
                            authViewModel.register(email, password)
                        }
                    },
                    enabled = !uiState.isLoading
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
                    modifier = Modifier.clickable(enabled = !uiState.isLoading) { onNavigateToLogin() }
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    DearDiaryTheme {
        val fakeMainViewModel = com.psy.deardiary.features.main.MainViewModel()
        RegisterScreen(
            onNavigateToLogin = {},
            onBackClick = {},
            mainViewModel = fakeMainViewModel
        )
    }
}
