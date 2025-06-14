// File: app/src/main/java/com/psy/deardiary/features/onboarding/OnboardingScreen.kt
// Deskripsi: Layar perkenalan yang menyambut pengguna baru. Menggunakan
// HorizontalPager untuk menciptakan alur perkenalan slide-based.

package com.psy.deardiary.features.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psy.deardiary.R
import com.psy.deardiary.ui.components.PrimaryButton
import com.psy.deardiary.ui.components.SecondaryButton
import com.psy.deardiary.ui.theme.DearDiaryTheme
import kotlinx.coroutines.launch

// Data untuk setiap halaman onboarding
data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    val pages = listOf(
        OnboardingPageData(
            imageRes = R.drawable.ic_launcher_foreground,
            title = stringResource(id = R.string.onboarding_welcome),
            description = "Dear Diary adalah tempat di mana kamu bisa menjadi dirimu sendiri, tanpa dihakimi."
        ),
        OnboardingPageData(
            imageRes = R.drawable.ic_launcher_foreground,
            title = "Tulis, Refleksi, dan Tumbuh",
            description = stringResource(id = R.string.onboarding_reflect)
        ),
        OnboardingPageData(
            imageRes = R.drawable.ic_launcher_foreground,
            title = "Privasi & Keamanan Terjamin",
            description = stringResource(id = R.string.onboarding_private)
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                // Memanggil fungsi yang hilang
                OnboardingPage(data = pages[pageIndex])
            }
            // Memanggil fungsi yang hilang
            OnboardingControls(
                pagerState = pagerState,
                onOnboardingComplete = onOnboardingComplete
            )
        }
    }
}

// --- FUNGSI YANG HILANG DITAMBAHKAN KEMBALI DI SINI ---
@Composable
fun OnboardingPage(data: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = data.imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = data.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingControls(
    pagerState: PagerState,
    onOnboardingComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Indikator halaman
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color)
                        .size(12.dp)
                )
            }
        }

        // Tombol Aksi
        if (pagerState.currentPage == pagerState.pageCount - 1) {
            PrimaryButton(
                text = "Mulai Sekarang",
                onClick = onOnboardingComplete,
                modifier = Modifier.width(180.dp)
            )
        } else {
            SecondaryButton(
                text = "Lanjut",
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier.width(180.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    DearDiaryTheme {
        OnboardingScreen(onOnboardingComplete = {})
    }
}
