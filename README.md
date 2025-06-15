# 🌿 Dear Diary
> Aplikasi Pendamping Kesehatan Mental yang Interaktif & Komprehensif

---

## 🎯 Gambaran Umum
**Dear Diary** merupakan aplikasi kesehatan mental yang tidak sekadar menyediakan jurnal harian, namun juga terintegrasi dengan berbagai fitur penting untuk mendukung pertumbuhan pribadi dan kesejahteraan emosional. Dengan kombinasi desain modern, intuitif, dan konten berkualitas tinggi, Dear Diary membantu pengguna membangun kebiasaan positif dan mendapatkan bantuan profesional dengan lebih mudah.

---

## ✨ Fitur Utama

### 🏠 Tab Beranda
- **Quick Input Card**: Tulis jurnal harian dengan mudah melalui kartu input intuitif, mirip gaya Facebook.
- **Feed Jurnal Harian**: Tampilkan riwayat jurnal Anda dengan desain yang rapi dan nyaman dibaca.
- **Pengaturan Cepat**: Akses pengaturan aplikasi langsung dari layar utama.

### 🎵 Tab Media
- **Musik & Suara Alam**: Playlist relaksasi untuk membantu meditasi dan mengurangi stres.
- **Artikel & Blog**: Artikel bermanfaat terkait kesehatan mental dari sumber tepercaya.
- **Jurnal Terpandu**: Prompt khusus yang memandu pengguna ketika kesulitan menulis jurnal.

### ❤️ Tab Layanan
- **Tes Psikologi**:
  - Tes Kepribadian (MBTI)
  - Beck Depression Inventory (BDI)
  - Tes Minat & Bakat
- **Direktori Psikolog Profesional**: Temukan dan hubungi psikolog berpengalaman.
- **Tombol Bantuan Darurat**: Akses cepat ke layanan bantuan krisis.

### 🌳 Tab Pertumbuhan
- **Pohon Kehidupan**: Visualisasi unik yang tumbuh seiring aktivitas positif pengguna.
- **Kalender Mood**: Pantau mood harian secara visual melalui kalender intuitif.
- **Statistik & Pencapaian**: Lihat perkembangan pribadi, total jurnal, runtutan konsistensi, dan lencana penghargaan.

---

## 📱 Visualisasi Navigasi & UI
+------------------------------------------+
| Dear Diary App (Login) |
| |
| +--------------------------------------+ |
| | | |
| | KONTEN HALAMAN AKTIF | |
| | (Beranda/Media/Layanan/Growth) | |
| | | |
| +--------------------------------------+ |
| |
+------------------------------------------+
| [🏠] [🎵] [❤️] [🌳] |
| Beranda Media Layanan Pertumbuhan|
+------------------------------------------+


---

## 🚧 Teknologi yang Digunakan
- **Frontend:** Jetpack Compose (Android)
- **Database:** Room (Android Local DB)
- **Integrasi AI:** OpenRouter (AI Content Generation)

---

## 📌 Instalasi & Menjalankan Proyek


## Membangun Aplikasi Android

1. Pastikan Java Development Kit (JDK) 11 atau lebih baru telah terpasang.
2. Dari direktori root proyek jalankan:

   ```bash
   ./gradlew assembleDebug
   ```

   Atau gunakan `./gradlew installDebug` untuk langsung memasang aplikasi ke
   perangkat/emulator yang terhubung.

> **Catatan:** berkas `.jar` tidak dapat dieksekusi di lingkungan ini, termasuk `gradle-wrapper.jar`. Jalankan Gradle melalui instalasi lokal atau unduh jar tersebut secara manual bila diperlukan. Berkas wrapper telah diabaikan di `.gitignore`.

File APK hasil build dapat ditemukan di `app/build/outputs/apk/`.
