// File: app/src/main/java/com/psy/deardiary/ui/theme/Color.kt
// Deskripsi: Mendefinisikan palet warna aplikasi. Setiap warna dideklarasikan
// sebagai sebuah konstanta agar bisa digunakan kembali di seluruh aplikasi.

package com.psy.deardiary.ui.theme

import androidx.compose.ui.graphics.Color

// --- PALET WARNA LIGHT MODE ---

// Warna Utama & Turunannya
val Primary = Color(0xFFB0E0E6)
val OnPrimary = Color(0xFF2C3E50)
val PrimaryContainer = Color(0xFFE1F5FE)
val OnPrimaryContainer = Color(0xFF2C3E50)

// Warna Sekunder
val Secondary = Color(0xFFC8E6C9)
val OnSecondary = Color(0xFF1B5E20)
val SecondaryContainer = Color(0xFFDCEDC8)
val OnSecondaryContainer = Color(0xFF1B5E20)

// Warna Permukaan & Latar Belakang
val Surface = Color(0xFFF7F9F9)
val OnSurface = Color(0xFF333333)
val OnSurfaceVariant = Color(0xFF757575)
val Outline = Color(0xFFBDBDBD)

// Warna Status Error & Peringatan
val Error = Color(0xFFE57373)
val OnError = Color(0xFFFFFFFF)

// Warna Khusus
val Crisis = Color(0xFFFFAB91)
val OnCrisis = Color(0xFF2C3E50)


// --- PENAMBAHAN BARU: PALET WARNA DARK MODE ---

val DarkPrimary = Color(0xFF82B1FF) // Biru yang lebih lembut untuk mode gelap
val DarkOnPrimary = Color(0xFF001E3C)
val DarkPrimaryContainer = Color(0xFF004494)
val DarkOnPrimaryContainer = Color(0xFFDDE7FF)

val DarkSecondary = Color(0xFFA5D6A7) // Hijau yang lebih lembut
val DarkOnSecondary = Color(0xFF0D3210)
val DarkSecondaryContainer = Color(0xFF33691E)
val DarkOnSecondaryContainer = Color(0xFFC8E6C9)

val DarkSurface = Color(0xFF1E1E1E) // Latar belakang abu-abu gelap
val DarkOnSurface = Color(0xFFE0E0E0) // Teks putih keabuan
val DarkOnSurfaceVariant = Color(0xFFBDBDBD) // Teks sekunder yang lebih redup
val DarkOutline = Color(0xFF5F5F5F)

val DarkError = Color(0xFFFF8A80)
val DarkOnError = Color(0xFF410001)

val DarkCrisis = Color(0xFFFFB59E)
val DarkOnCrisis = Color(0xFF3E0E00)