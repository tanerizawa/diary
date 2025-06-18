# Lokasi: ./app/core/config.py

import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "Dear Diary API"
    API_V1_STR: str = "/api/v1"

    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./test.db")

    SECRET_KEY: str = os.getenv("SECRET_KEY", "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")
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
