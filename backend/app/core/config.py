# Lokasi: ./app/core/config.py

import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "Dear Diary API"
    API_V1_STR: str = "/api/v1"

    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./test.db")

    SECRET_KEY: str = os.getenv("SECRET_KEY", "a_very_secret_key_that_should_be_changed")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 8 # 8 hari

    # --- PENAMBAHAN BARU ---
    # Konfigurasi untuk Layanan AI (contoh menggunakan OpenRouter)
    # Ambil API Key dari environment variable
    AI_API_KEY: str = os.getenv("AI_API_KEY", "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")
    AI_API_URL: str = "https://openrouter.ai/api/v1/chat/completions"
    # --- AKHIR PENAMBAHAN ---

    class Config:
        case_sensitive = True

settings = Settings()
