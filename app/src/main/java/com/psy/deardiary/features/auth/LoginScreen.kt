// File: app/src/main/java/com/psy/deardiary/features/auth/LoginScreen.kt
// Deskripsi: Layar untuk pengguna masuk ke aplikasi.
// VERSI DIPERBARUI: Ditambahkan indikator loading dan penanganan error.

package com.psy.deardiary.features.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.repository.AuthRepository
import com.psy.deardiary.ui.components.PasswordTextField
import com.psy.deardiary.ui.components.PrimaryButton
import com.psy.deardiary.ui.components.PrimaryTextField
import com.psy.deardiary.ui.components.InfoDialog
import com.psy.deardiary.ui.theme.DearDiaryTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.psy.deardiary.data.network.AuthApiService

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    // Menghapus parameter onLoginClick karena logika akan ditangani oleh ViewModel
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by authViewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    // PERBAIKAN: Typo 'mutableStateof' menjadi 'mutableStateOf'
    var password by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        showErrorDialog = uiState.errorMessage != null
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    text = "Selamat Datang Kembali",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = "Masuk untuk melanjutkan perjalananmu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(48.dp))
                PrimaryTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    // PERBAIKAN: Komponen PrimaryTextField tidak memiliki parameter 'enabled', jadi dihapus.
                    // Jika fungsionalitas ini diinginkan, parameter 'enabled' harus ditambahkan ke definisi komponen tersebut.
                )
                Spacer(modifier = Modifier.height(16.dp))
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    // PERBAIKAN: Komponen PasswordTextField tidak memiliki parameter 'enabled', jadi dihapus.
                )
                Spacer(modifier = Modifier.height(32.dp))
                PrimaryButton(
                    text = "Login",
                    onClick = { authViewModel.login(email, password) },
                    enabled = !uiState.isLoading // PrimaryButton memiliki parameter 'enabled'
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = buildAnnotatedString {
                        append("Belum punya akun? ")
                        withStyle(style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )) {
                            append("Daftar di sini")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable(enabled = !uiState.isLoading) {
                        onNavigateToRegister()
                    }
                )
            }

            // Tampilkan Indikator Loading di tengah layar
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            if (showErrorDialog) {
                InfoDialog(
                    onDismissRequest = {
                        showErrorDialog = false
                        authViewModel.clearErrorMessage()
                    },
                    title = "Login Gagal",
                    text = uiState.errorMessage ?: "Terjadi kesalahan"
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    DearDiaryTheme {
        // PERBAIKAN: Memberikan implementasi palsu (mock/fake) untuk dependensi agar Preview bisa berjalan.
        // Ini adalah praktik standar untuk preview Composable yang menggunakan ViewModel dengan Hilt.
        val context = LocalContext.current
        val fakeAuthApiService = Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)

        val fakeUserPrefsRepo = UserPreferencesRepository(context)
        val fakeAuthRepo = AuthRepository(fakeAuthApiService, fakeUserPrefsRepo)
        val fakeViewModel = AuthViewModel(fakeAuthRepo)

        LoginScreen(onNavigateToRegister = {}, authViewModel = fakeViewModel)
    }
}
