package com.psy.deardiary.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font // <-- IMPORT DIUBAH
import androidx.compose.ui.text.googlefonts.GoogleFont // <-- IMPORT BARU
import androidx.compose.ui.unit.sp
import com.psy.deardiary.R
// import com.psy.deardiary.R <-- TIDAK PERLU LAGI R.font

// 1. Siapkan provider Google Fonts
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs // <-- Ini merujuk ke meta-data di manifest
)

// 2. Tentukan nama font yang ingin digunakan
private val fontName = GoogleFont("Nunito Sans")

// 3. Buat FontFamily menggunakan provider
// Kode menjadi jauh lebih simpel!
private val NunitoSans = FontFamily(
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Bold)
)

// 4. Objek Typography tetap sama, tidak perlu diubah
val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // ... gaya teks lainnya
)