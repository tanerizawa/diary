// File: app/src/main/java/com/psy/deardiary/ui/theme/Theme.kt
// Deskripsi: File ini adalah jantung dari Design System. Ia menggabungkan
// palet warna dan tipografi menjadi sebuah tema Material 3 yang utuh.

package com.psy.deardiary.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.colorSchemeFromSeed
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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

// --- KODE YANG DIPERBAIKI ---
// Skema warna gelap sekarang menggunakan palet warna gelap yang telah didefinisikan
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    error = DarkError,
    onError = DarkOnError,
    outline = DarkOutline
)
// --- AKHIR KODE YANG DIPERBAIKI ---

@Composable
fun DearDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    seedColor: Color = Primary,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        dynamicColor -> {
            colorSchemeFromSeed(seedColor = seedColor, darkTheme = darkTheme)
        }
        else -> if (darkTheme) DarkColorScheme else LightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Mengatur warna status bar agar sesuai dengan warna primer tema
            window.statusBarColor = colorScheme.primary.toArgb()
            // Atur warna ikon status bar berdasarkan tema
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}