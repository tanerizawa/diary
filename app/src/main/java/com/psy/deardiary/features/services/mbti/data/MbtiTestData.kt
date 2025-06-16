// File Baru: app/src/main/java/com/psy/deardiary/features/services/mbti/MbtiTestData.kt
// Deskripsi: Menyimpan semua data statis untuk pertanyaan dan hasil tes MBTI.

package com.psy.deardiary.features.services.mbti.data

// Enum untuk merepresentasikan empat dimensi MBTI
enum class MbtiDimension {
    INTROVERSION_EXTRAVERSION, // I/E
    SENSING_INTUITION,         // S/N
    THINKING_FEELING,          // T/F
    JUDGING_PERCEIVING         // J/P
}

// Model untuk setiap pertanyaan
data class MbtiQuestion(
    val text: String,
    val dimension: MbtiDimension,
    val scoresForIntrovert: Boolean // Jika 'true', jawaban "Ya" memberi skor ke I,S,T,J
)

// Daftar pertanyaan (contoh singkat)
val mbtiQuestions = listOf(
    MbtiQuestion("Anda lebih suka menghabiskan waktu di lingkungan yang tenang?", MbtiDimension.INTROVERSION_EXTRAVERSION, true),
    MbtiQuestion("Anda sering merasa bersemangat setelah bersosialisasi dengan banyak orang?", MbtiDimension.INTROVERSION_EXTRAVERSION, false),
    MbtiQuestion("Anda lebih fokus pada detail dan fakta nyata daripada konsep abstrak?", MbtiDimension.SENSING_INTUITION, true),
    MbtiQuestion("Anda sering memikirkan kemungkinan dan makna tersembunyi?", MbtiDimension.SENSING_INTUITION, false),
    MbtiQuestion("Saat membuat keputusan, Anda lebih mengutamakan logika daripada perasaan orang lain?", MbtiDimension.THINKING_FEELING, true),
    MbtiQuestion("Anda sangat mempertimbangkan harmoni dan empati saat mengambil keputusan?", MbtiDimension.THINKING_FEELING, false),
    MbtiQuestion("Anda lebih suka memiliki rencana yang jelas dan terstruktur?", MbtiDimension.JUDGING_PERCEIVING, true),
    MbtiQuestion("Anda lebih suka bersikap spontan dan fleksibel terhadap rencana?", MbtiDimension.JUDGING_PERCEIVING, false)
)

// Deskripsi untuk setiap tipe kepribadian
val mbtiResults = mapOf(
    "ISTJ" to "Sang Penginspeksi: Praktis, faktual, dan bertanggung jawab.",
    "ISFJ" to "Sang Pelindung: Hangat, teliti, dan berdedikasi.",
    "INFJ" to "Sang Penasihat: Penuh wawasan, idealis, dan teguh.",
    "INTJ" to "Sang Arsitek: Imajinatif, strategis, dan perencana.",
    "ISTP" to "Sang Pengrajin: Logis, pemecah masalah yang handal.",
    "ISFP" to "Sang Seniman: Menawan, sensitif, dan penuh gairah.",
    "INFP" to "Sang Mediator: Puitis, baik hati, dan altruistik.",
    "INTP" to "Sang Pemikir: Inovatif, haus akan pengetahuan.",
    "ESTP" to "Sang Pembujuk: Cerdas, energik, dan perseptif.",
    "ESFP" to "Sang Penghibur: Spontan, antusias, dan suka bersenang-senang.",
    "ENFP" to "Sang Juara: Kreatif, bersemangat, dan mudah bergaul.",
    "ENTP" to "Sang Pendebat: Cerdas, penasaran, dan tidak bisa diam.",
    "ESTJ" to "Sang Eksekutif: Terorganisir, administrator yang hebat.",
    "ESFJ" to "Sang Konsul: Peduli, sosial, dan populer.",
    "ENFJ" to "Sang Protagonis: Karismatik, pemimpin yang menginspirasi.",
    "ENTJ" to "Sang Komandan: Berani, imajinatif, dan pemimpin yang kuat."
)
