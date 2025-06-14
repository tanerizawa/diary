# File: app/database.py
# Deskripsi: Mengatur koneksi ke database menggunakan SQLAlchemy.

from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

from app.core.config import settings # <- Impor settings

# Membuat engine SQLAlchemy menggunakan URL dari settings
engine = create_engine(settings.DATABASE_URL)

# Membuat sesi database lokal yang akan digunakan di seluruh aplikasi
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Base class yang akan diwarisi oleh semua model database
Base = declarative_base()

def get_db():
    """
    Dependency untuk mendapatkan sesi database per request.
    Ini memastikan sesi database selalu ditutup setelah request selesai.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()