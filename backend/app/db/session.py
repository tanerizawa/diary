# Lokasi: ./app/db/session.py
# Deskripsi: Menginisialisasi engine dan session maker untuk koneksi database.

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from app.core.config import settings

# Argumen connect_args diperlukan untuk SQLite
engine = create_engine(
    settings.DATABASE_URL,
    connect_args={"check_same_thread": False} if "sqlite" in settings.DATABASE_URL else {}
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
