// File Baru: app/src/main/java/com/psy/deardiary/utils/AudioPlayer.kt
// Deskripsi: Kelas helper untuk mengelola MediaPlayer.

package com.psy.deardiary.utils

import android.media.MediaPlayer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlayer @Inject constructor() {

    private var player: MediaPlayer? = null

    // Fungsi untuk memeriksa apakah URL tertentu sedang diputar
    fun isPlaying(url: String): Boolean {
        return player?.isPlaying == true && player?.currentDataSource == url
    }

    // Fungsi untuk memulai pemutaran atau menghentikan jika lagu yang sama diputar lagi
    fun play(url: String, onCompletion: () -> Unit) {
        if (isPlaying(url)) {
            stop(onCompletion)
            return
        }

        // Hentikan pemutaran sebelumnya jika ada
        stop(onCompletion)

        player = MediaPlayer().apply {
            try {
                setDataSource(url)
                setOnPreparedListener { start() }
                setOnCompletionListener {
                    // Panggil stop untuk merilis resource dan memanggil callback
                    stop(onCompletion)
                }
                prepareAsync()
            } catch (e: Exception) {
                // Tangani error, misalnya URL tidak valid
                stop(onCompletion)
            }
        }
    }

    // Fungsi untuk menghentikan pemutaran
    fun stop(onCompletion: () -> Unit) {
        player?.release()
        player = null
        onCompletion() // Selalu panggil callback untuk update UI
    }
}
// Tambahan untuk MediaPlayer agar bisa mendapatkan data source
private val MediaPlayer.currentDataSource: String?
    get() = try {
        this::class.java.getDeclaredField("mPath").apply { isAccessible = true }.get(this) as? String
    } catch (e: Exception) {
        null
    }
