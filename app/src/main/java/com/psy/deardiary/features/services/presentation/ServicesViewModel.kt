// File Baru: app/src/main/java/com/psy/deardiary/features/services/ServicesViewModel.kt
// Deskripsi: ViewModel untuk mengelola data dan state pada ServicesScreen.

package com.psy.deardiary.features.services.presentation

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.psy.deardiary.ui.theme.Primary
import com.psy.deardiary.ui.theme.Secondary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Data class untuk setiap item layanan
data class ServiceItem(
    val title: String,
    val description: String,
    val icon: String, // Emoji
    val color: Color
)

// State untuk ServicesScreen
data class ServicesUiState(
    val availableTests: List<ServiceItem>,
    val professionalServices: List<ServiceItem>
)

@HiltViewModel
class ServicesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(
        ServicesUiState(
            availableTests = listOf(
                ServiceItem(
                    title = "Tes Kepribadian (MBTI)",
                    description = "Pahami tipe kepribadianmu lebih dalam.",
                    icon = "🧠",
                    color = Primary
                ),
                ServiceItem(
                    title = "Tes Tingkat Stres (DASS-21)",
                    description = "Ukur tingkat depresi, kecemasan, dan stres.",
                    icon = "😥",
                    color = Secondary
                )
            ),
            professionalServices = listOf(
                ServiceItem(
                    title = "Direktori Psikolog",
                    description = "Temukan bantuan profesional di dekatmu.",
                    icon = "👩‍⚕️",
                    color = Color(0xFFffb4a2)
                )
            )
        )
    )
    val uiState = _uiState.asStateFlow()
}
