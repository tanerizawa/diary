# diary
Dear Diary adalah aplikasi jurnal pribadi untuk Android yang dirancang untuk membantu Anda merefleksikan hari Anda dengan aman dan pribadi. Dibangun dengan Jetpack Compose modern dan filosofi offline-first, aplikasi ini memastikan pikiran dan emosi Anda selalu dapat diakses, kapan pun dan di mana pun.

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

## Konfigurasi Base URL API

URL backend sekarang diatur melalui BuildConfig sehingga dapat berbeda
antara build *debug* dan *release*.

Secara bawaan, file `app/build.gradle.kts` mendefinisikan:

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
    }
    release {
        buildConfigField("String", "BASE_URL", "\"https://api.example.com/\"")
    }
}
```

Saat pengembangan menggunakan emulator, biarkan nilai debug seperti di atas.
Untuk produksi, ganti `https://api.example.com/` dengan alamat server Anda.
