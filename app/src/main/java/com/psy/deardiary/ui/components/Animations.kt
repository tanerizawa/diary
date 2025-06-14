// File Baru: app/src/main/java/com/psy/deardiary/ui/components/Animations.kt
// Deskripsi: Berisi komponen-komponen yang dapat digunakan kembali untuk
// menerapkan animasi pada elemen UI lainnya secara mudah.

package com.psy.deardiary.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Sebuah wrapper yang akan membuat konten di dalamnya muncul dengan
 * efek fade-in dan slide-in dari bawah secara bertahap.
 *
 * @param modifier Modifier untuk diterapkan pada wrapper.
 * @param content Composable yang ingin dianimasikan.
 */
@Composable
fun AnimatedFadeIn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible.value = true
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible.value,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                slideInVertically(
                    initialOffsetY = { it / 10 },
                    animationSpec = tween(durationMillis = 500)
                )
    ) {
        content()
    }
}