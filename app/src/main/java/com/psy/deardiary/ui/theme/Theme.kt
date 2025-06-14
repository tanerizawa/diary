// File: app/src/main/java/com/psy/deardiary/ui/theme/Theme.kt
// Deskripsi: File ini adalah jantung dari Design System. Ia menggabungkan
// palet warna dan tipografi menjadi sebuah tema Material 3 yang utuh.

package com.psy.deardiary.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Mendefinisikan skema warna terang menggunakan warna yang telah kita buat di Color.kt
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    surface = Surface,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    background = Surface, // Latar belakang utama sama dengan surface
    onBackground = OnSurface,
    error = Error,
    onError = OnError,
    outline = Outline
)

// TODO: Definisikan DarkColorScheme di sini jika aplikasi akan mendukung mode gelap

@Composable
fun DearDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Saat ini kita hanya menggunakan skema warna terang
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Mengatur warna status bar agar sesuai dengan warna primer tema
            window.statusBarColor = colorScheme.primary.toArgb()
            // Mengatur agar ikon di status bar (jam, baterai) berwarna gelap
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}