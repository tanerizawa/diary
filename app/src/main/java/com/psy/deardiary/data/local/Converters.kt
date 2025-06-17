// File: app/src/main/java/com/psy/deardiary/data/local/Converters.kt
// Deskripsi: Type converter untuk Room, agar bisa menyimpan tipe data kompleks
// seperti List<String> ke dalam kolom database tunggal.

package com.psy.deardiary.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return value.split(",").map { it.trim() }
    }
}
