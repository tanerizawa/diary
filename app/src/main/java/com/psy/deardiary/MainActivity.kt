package com.psy.deardiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.psy.deardiary.navigation.AppNavigation
import com.psy.deardiary.ui.theme.DearDiaryTheme
import dagger.hilt.android.AndroidEntryPoint

// Anotasi @AndroidEntryPoint wajib ada jika Anda menggunakan Hilt
// untuk dependency injection (misalnya, untuk ViewModel).
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Membungkus seluruh aplikasi dengan tema yang sudah Anda definisikan.
            // Ini akan menerapkan warna, tipografi, dan bentuk (shapes) yang konsisten.
            DearDiaryTheme {
                // Surface adalah container dasar dari Material Design.
                // Menggunakan ini akan menerapkan warna latar belakang dari tema Anda.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. Membuat NavController.
                    // 2. Menggunakan rememberNavController() agar state navigasi
                    //    tetap terjaga saat terjadi recomposition (misal: rotasi layar).
                    val navController = rememberNavController()

                    // 3. Memanggil AppNavigation, yang menjadi "peta jalan" atau
                    //    grafik navigasi untuk seluruh aplikasi Anda.
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}