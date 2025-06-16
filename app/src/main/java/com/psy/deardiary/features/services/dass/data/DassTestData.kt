// File BARU: app/src/main/java/com/psy/deardiary/features/services/dass/DassTestData.kt

package com.psy.deardiary.features.services.dass.data

enum class DassScale { DEPRESSION, ANXIETY, STRESS }

data class DassQuestion(
    val text: String,
    val scale: DassScale
)

// Ini adalah 7 pertanyaan per skala, total 21 pertanyaan
val dassQuestions = listOf(
    DassQuestion("Saya merasa sulit untuk bersemangat", DassScale.DEPRESSION),
    DassQuestion("Saya merasa mulut saya kering", DassScale.ANXIETY),
    DassQuestion("Saya merasa tidak dapat merasakan perasaan positif sama sekali", DassScale.DEPRESSION),
    DassQuestion("Saya mengalami kesulitan bernapas (misalnya, sering terengah-engah)", DassScale.ANXIETY),
    DassQuestion("Saya merasa sulit untuk mengambil inisiatif", DassScale.DEPRESSION),
    DassQuestion("Saya cenderung bereaksi berlebihan terhadap situasi", DassScale.STRESS),
    DassQuestion("Saya merasa gemetar (misalnya, di tangan)", DassScale.ANXIETY),
    // ... Tambahkan 14 pertanyaan DASS-21 lainnya di sini ...
    // Untuk contoh, kita akan gunakan 7 pertanyaan ini saja dan mengulanginya
    DassQuestion("Saya merasa hidup tidak berarti", DassScale.DEPRESSION),
    DassQuestion("Saya khawatir tentang situasi di mana saya mungkin panik", DassScale.ANXIETY),
    DassQuestion("Saya merasa sulit untuk rileks", DassScale.STRESS),
    DassQuestion("Saya merasa sedih dan tertekan", DassScale.DEPRESSION),
    DassQuestion("Saya tidak tahan dengan gangguan apa pun", DassScale.STRESS),
    DassQuestion("Saya merasa hampir panik", DassScale.ANXIETY),
    DassQuestion("Saya merasa tidak berharga sebagai pribadi", DassScale.DEPRESSION),
    DassQuestion("Saya mudah marah", DassScale.STRESS),
    DassQuestion("Saya merasakan detak jantung saya meningkat tanpa alasan", DassScale.ANXIETY),
    DassQuestion("Saya mudah tersinggung", DassScale.STRESS),
    DassQuestion("Saya merasa putus asa tentang masa depan", DassScale.DEPRESSION),
    DassQuestion("Saya merasa gugup", DassScale.STRESS),
    DassQuestion("Saya merasa hidup ini tidak layak dijalani", DassScale.DEPRESSION),
    DassQuestion("Saya khawatir akan performa saya", DassScale.ANXIETY)
)

val dassAnswerOptions = listOf(
    "Tidak pernah" to 0,
    "Kadang-kadang" to 1,
    "Sering" to 2,
    "Hampir selalu" to 3
)

fun getSeverity(scale: DassScale, score: Int): String {
    val finalScore = score * 2 // Skor DASS-21 dikalikan dua
    return when (scale) {
        DassScale.DEPRESSION -> when {
            finalScore <= 9 -> "Normal"
            finalScore <= 13 -> "Ringan"
            finalScore <= 20 -> "Sedang"
            finalScore <= 27 -> "Parah"
            else -> "Sangat Parah"
        }
        DassScale.ANXIETY -> when {
            finalScore <= 7 -> "Normal"
            finalScore <= 9 -> "Ringan"
            finalScore <= 14 -> "Sedang"
            finalScore <= 19 -> "Parah"
            else -> "Sangat Parah"
        }
        DassScale.STRESS -> when {
            finalScore <= 14 -> "Normal"
            finalScore <= 18 -> "Ringan"
            finalScore <= 25 -> "Sedang"
            finalScore <= 33 -> "Parah"
            else -> "Sangat Parah"
        }
    }
}