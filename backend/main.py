from dotenv import load_dotenv  # <-- 1. TAMBAHKAN IMPORT INI
load_dotenv()                   # <-- 2. PANGGIL FUNGSI INI DI SINI

from app.core import logging as log_setup
log_setup.setup_logging()

from fastapi import FastAPI
from app.api.v1.api import api_router
from app.core.config import settings
# Database schema now managed via Alembic migrations

app = FastAPI(title=settings.PROJECT_NAME)

app.include_router(api_router, prefix=settings.API_V1_STR)

@app.get("/")
def read_root():
    """Endpoint root untuk verifikasi bahwa server berjalan."""
    return {"message": f"Welcome to {settings.PROJECT_NAME}"}

