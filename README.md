# 🌿 Dear Diary
> Aplikasi Pendamping Kesehatan Mental yang Interaktif & Komprehensif

---

## 🎯 Gambaran Umum
**Dear Diary** merupakan aplikasi kesehatan mental yang tidak sekadar menyediakan jurnal harian, namun juga terintegrasi dengan berbagai fitur penting untuk mendukung pertumbuhan pribadi dan kesejahteraan emosional. Dengan kombinasi desain modern, intuitif, dan konten berkualitas tinggi, Dear Diary membantu pengguna membangun kebiasaan positif dan mendapatkan bantuan profesional dengan lebih mudah.

---

## ✨ Fitur Utama

### 🏠 Tab Beranda
- **Catatan Singkat**: Tulis pesan pendek tanpa judul seperti di Twitter melalui panel input cepat.
- **Feed Jurnal Harian**: Tampilkan riwayat jurnal Anda dengan desain yang rapi dan nyaman dibaca.
- **Pengaturan Cepat**: Akses pengaturan aplikasi langsung dari layar utama.

### 🎵 Tab Media
- **Musik & Suara Alam**: Playlist relaksasi untuk membantu meditasi dan mengurangi stres.
- **Artikel & Blog**: Artikel bermanfaat terkait kesehatan mental dari sumber tepercaya.
- **Jurnal Terpandu**: Prompt khusus yang memandu pengguna ketika kesulitan menulis jurnal.
- **Menulis Jurnal Panjang**: Akses editor penuh untuk menulis essai atau blog pribadi.

### ❤️ Tab Layanan
- **Tes Psikologi**:
  - Tes Kepribadian (MBTI)
  - Tes Tingkat Stres (DASS-21)
- **Direktori Psikolog Profesional**: Temukan dan hubungi psikolog berpengalaman.
- **Tombol Bantuan Darurat**: Akses cepat ke layanan bantuan krisis.

### 🌳 Tab Pertumbuhan
- **Pohon Ketenangan**: Visualisasi unik yang tumbuh seiring aktivitas positif pengguna.
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

## Menjalankan Backend

Backend menggunakan FastAPI dan SQLAlchemy. Skema database dikelola melalui Alembic.

### Konfigurasi Environment

1. Salin berkas `.env.example` di direktori root menjadi `.env` dan isi variabel di bawah ini:

   - `DATABASE_URL` – URL koneksi database
   - `SECRET_KEY` – kunci rahasia untuk JWT
   - `AI_API_KEY` – kunci API layanan AI
   - `AI_API_URL` – endpoint layanan AI
   - `AI_MODEL` – model AI yang digunakan

   Contoh nilai default dapat dilihat pada berkas `.env.example`.

   > **Catatan**: File `.env` berisi informasi sensitif dan telah dimasukkan ke `.gitignore`. Pastikan Anda tidak meng-commit file ini ke repository publik.

2. Install dependensi Python:

   ```bash
   pip install -r backend/requirements.txt
   ```

3. Dari direktori *root* repository, jalankan migrasi database (database SQLite
   akan dibuat sesuai nilai `DATABASE_URL`, default-nya `dear_diary.db` di
   direktori root). Jalankan langkah ini setiap kali Anda menarik perubahan yang
   menambah atau mengubah skema database:

   ```bash
   alembic -c backend/alembic.ini upgrade head
   ```

   Jika Anda memodifikasi model sendiri, buat berkas migrasi baru lebih dulu
   dengan perintah:

   ```bash
   alembic -c backend/alembic.ini revision --autogenerate -m "Pesan migrasi"
   ```
   Setelah itu jalankan perintah `upgrade` di atas.

4. Mulai server pengembangan:

   ```bash
   uvicorn backend.main:app --reload
   ```

### Troubleshooting

Jika saat menjalankan backend Anda melihat pesan `sqlite3.OperationalError: no such table: users`,
pastikan perintah migrasi dijalankan dari direktori *root* sehingga file database sesuai `DATABASE_URL`
(misalnya `dear_diary.db`) dibuat pada lokasi yang benar:

```bash
alembic -c backend/alembic.ini upgrade head
```

## Running Tests

1. Install dependensi Python beserta `pytest`:

   ```bash
   pip install -r backend/requirements.txt pytest
   ```

2. Jalankan seluruh tes unit dari direktori root:

   ```bash
   pytest
   ```

## Cara Kerja Analisis Sentimen Chat

Endpoint `/api/v1/chat/` tidak hanya mengirimkan pesan Anda ke model AI, namun
juga melakukan analisis sentimen. Urutannya sebagai berikut:

1. Pesan pengguna disimpan di tabel `chatmessages`.
2. Fungsi `analyze_sentiment_with_ai` memanggil layanan AI dan
   mengembalikan `sentiment_score` serta `key_emotions`.
3. Nilai tersebut dikirim kembali bersama balasan AI dan disimpan kembali
   secara asinkron lewat `process_and_update_sentiment`.

Hasil analisis dapat dilihat pada kolom `sentiment_score` dan `key_emotions`
di database.

Mulai versi ini, Anda juga dapat menghapus beberapa pesan sekaligus melalui
endpoint:

```http
DELETE /api/v1/chat/messages
```

Kirimkan body JSON `{ "ids": [1, 2, 3] }` untuk menghapus pesan dengan ID
tertentu. Pesan yang dihapus akan hilang dari basis data dan tidak muncul lagi
di riwayat percakapan.

### Analisis Percakapan Terkini

Endpoint ini kini meninjau beberapa pesan terakhir sebelum memberikan balasan.
Bila relevan, AI dapat menutup jawabannya dengan pertanyaan singkat untuk
mendorong interaksi. Pertanyaan tidak akan diulang apabila pada balasan
sebelumnya AI sudah menanyakannya.

### Prompt Otomatis dari AI

Tab Beranda kini dapat memunculkan pesan pembuka otomatis ketika pengguna lama
tidak berinteraksi. Backend menyediakan endpoint `/api/v1/chat/prompt` yang
mengumpulkan riwayat jurnal dan percakapan lalu menghasilkan sapaan singkat yang
selalu diakhiri pertanyaan probing. Endpoint ini menyimpan pesan tersebut dan
membatasi pemanggilan jika dalam 6 jam terakhir sudah ada prompt serupa.

### Konteks Dinamis Chat

Balasan dari endpoint `/api/v1/chat/` kini mempertimbangkan konteks profil.
Informasi nama, bio, MBTI (jika ada), waktu saat ini, statistik mood, riwayat
jurnal terkini, serta percakapan terakhir disusun menjadi satu string dan
dikirim ke model AI. Hal ini membuat respons terasa lebih personal dan relevan.
