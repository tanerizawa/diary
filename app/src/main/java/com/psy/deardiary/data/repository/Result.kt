// File: app/src/main/java/com/psy/deardiary/data/repository/Result.kt
package com.psy.deardiary.data.repository

/**
 * Sebuah sealed class untuk membungkus hasil dari operasi yang bisa gagal.
 * @param T Tipe data yang diharapkan jika operasi berhasil.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}
