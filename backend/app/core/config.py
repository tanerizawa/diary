from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    # --- Pengaturan Proyek ---
    PROJECT_NAME: str = "Dear Diary API"
    API_V1_STR: str = "/api/v1"

    # --- Pengaturan Keamanan & Token ---
    DATABASE_URL: str
    SECRET_KEY: str
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 8  # Token berlaku selama 8 hari

    # --- Pengaturan Layanan AI ---
    # Semua variabel ini WAJIB diisi di dalam file .env
    AI_API_KEY: str
    AI_API_URL: str
    AI_MODEL: str  # Perbaikan untuk error 'AttributeError' sebelumnya

    # Konfigurasi Pydantic untuk memuat dari file .env di direktori yang sama
    # saat aplikasi dijalankan (yaitu dari dalam folder 'backend')
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8"
    )

settings = Settings()
