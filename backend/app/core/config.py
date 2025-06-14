# File: app/core/config.py
# Deskripsi: Mengelola semua konfigurasi aplikasi menggunakan Pydantic Settings.
# Memuat variabel dari environment atau file .env.

from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    """
    Kelas untuk menampung semua variabel konfigurasi aplikasi.
    """
    # Konfigurasi Database
    DATABASE_URL: str

    # Konfigurasi JWT
    SECRET_KEY: str
    ALGORITHM: str
    ACCESS_TOKEN_EXPIRE_MINUTES: int

    # Kunci API Eksternal
    OPENROUTER_API_KEY: str

    # Konfigurasi untuk memuat dari file .env
    model_config = SettingsConfigDict(env_file=".env")

# Buat satu instance settings yang akan digunakan di seluruh aplikasi
settings = Settings()