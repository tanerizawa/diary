// File: app/src/main/java/com/psy/deardiary/DearDiaryApplication.kt
package com.psy.deardiary

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DearDiaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
