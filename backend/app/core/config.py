from pydantic import field_validator
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
    # Model khusus untuk perencana percakapan dan generator respons
    AI_PLANNER_MODEL: str
    AI_GENERATOR_MODEL: str
    AI_HTTP_REFERER: str = "https://github.com/tanerizawa/diary"
    AI_TITLE: str = "Dear Diary App"
    # Optional path to planner config YAML
    PLANNER_CONFIG_FILE: str | None = None

    @field_validator("AI_API_KEY")
    @classmethod
    def check_api_key(cls, v: str) -> str:
        if not v or v == "CHANGE_ME":
            raise ValueError("AI_API_KEY must be set to a real API key")
        return v

    # Konfigurasi Pydantic untuk memuat dari file .env di direktori yang sama
    # saat aplikasi dijalankan (yaitu dari dalam folder 'backend')
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

settings = Settings()
