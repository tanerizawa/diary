// File: app/src/main/java/com/psy/deardiary/utils/AudioRecorder.kt
// Deskripsi: Kelas helper untuk mengelola proses perekaman audio.
// VERSI DIPERBARUI: Menggunakan @Inject constructor dengan @ApplicationContext.

package com.psy.deardiary.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

// PERBAIKAN: Anotasi @Singleton dan @Inject ditambahkan.
// Ini adalah cara yang lebih disukai untuk memberitahu Hilt cara membuat instance kelas ini.
@Singleton
class AudioRecorder @Inject constructor(
    // Anotasi @ApplicationContext secara eksplisit memberi tahu Hilt
    // untuk menyediakan konteks aplikasi, yang menyelesaikan error binding.
    @ApplicationContext private val context: Context
) {
    private var recorder: MediaRecorder? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val attributedContext = context.createAttributionContext("record_audio")
            MediaRecorder(attributedContext)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    fun start(outputFile: File) {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()

            recorder = this
        }
    }

    fun stop() {
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }
}
