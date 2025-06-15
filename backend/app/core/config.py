# Lokasi: ./app/core/config.py
# Deskripsi: Mengelola semua konfigurasi aplikasi, mengambil nilai dari environment variables.

import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "Dear Diary API"
    API_V1_STR: str = "/api/v1"

    # Konfigurasi Database (ambil dari environment variables)
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./test.db")

    # Konfigurasi JWT (ambil dari environment variables)
    SECRET_KEY: str = os.getenv("SECRET_KEY", "a_very_secret_key_that_should_be_changed")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 8 # 8 hari

    class Config:
        case_sensitive = True

settings = Settings()