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
- **Backend:** FastAPI (Python)
- **Database:** Room (Android Local DB)
- **Integrasi AI:** OpenRouter (AI Content Generation)

---

## 📌 Instalasi & Menjalankan Proyek

### 🔧 Backend (FastAPI)
1. Masuk ke direktori backend:
```bash
cd backend


## Konfigurasi Lingkungan

Aplikasi backend menggunakan beberapa variabel environment yang dapat didefinisikan
melalui file `.env` pada root proyek. Contoh isi file `.env`:

```env
# URL database SQLAlchemy
DATABASE_URL=sqlite:///./deardiary.db

# Konfigurasi JWT
SECRET_KEY=change_this_secret
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=30

# Kunci API untuk layanan analisis AI
OPENROUTER_API_KEY=your_openrouter_key
```

Pastikan Anda mengganti nilai variabel di atas sesuai kebutuhan.

## Menjalankan Backend FastAPI

1. Masuk ke direktori `backend` dan pasang dependensi:

   ```bash
   cd backend
   pip install -r requirements.txt
   ```

2. Jalankan server pengembangan dengan uvicorn:

   ```bash
   uvicorn main:app --reload
   ```

Server akan berjalan pada `http://localhost:8000` dan emulator Android akan
mengaksesnya melalui `http://10.0.2.2:8000`.

## Membangun Aplikasi Android

1. Pastikan Java Development Kit (JDK) 11 atau lebih baru telah terpasang.
2. Dari direktori root proyek jalankan:

   ```bash
   ./gradlew assembleDebug
   ```

   Atau gunakan `./gradlew installDebug` untuk langsung memasang aplikasi ke
   perangkat/emulator yang terhubung.

File APK hasil build dapat ditemukan di `app/build/outputs/apk/`.
