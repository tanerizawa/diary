# File: main.py
# Deskripsi: Titik masuk utama aplikasi FastAPI. Menginisialisasi aplikasi dan
# menyertakan semua router dari modul-modul fitur.

from fastapi import FastAPI
from app.api.endpoints import users, journal
from app.database import engine, Base

# Membuat tabel di database jika belum ada
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="Dear Diary Backend API",
    description="API untuk mendukung aplikasi kesehatan mental Dear Diary.",
    version="1.0.0"
)

# Menyertakan router untuk setiap fitur
app.include_router(users.router, prefix="/api/v1", tags=["Users"])
app.include_router(journal.router, prefix="/api/v1", tags=["Journal"])

@app.get("/", tags=["Root"])
def read_root():
    """Endpoint root untuk memeriksa apakah server berjalan."""
    return {"message": "Welcome to Dear Diary API"}

# Untuk menjalankan server ini, gunakan perintah di terminal:
# uvicorn main:app --reload