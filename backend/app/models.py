# File: app/models.py
# Deskripsi: Mendefinisikan model-model tabel database menggunakan SQLAlchemy.

from sqlalchemy import Column, Integer, String, Text, ForeignKey, Boolean, BigInteger
from sqlalchemy.orm import relationship
from .database import Base

class User(Base):
    """Model untuk tabel pengguna."""
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_active = Column(Boolean, default=True)

    # Hubungan ke entri jurnal
    journal_entries = relationship("JournalEntry", back_populates="owner")

class JournalEntry(Base):
    """Model untuk tabel entri jurnal."""
    __tablename__ = "journal_entries"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    content = Column(Text, nullable=False)
    mood = Column(String, nullable=False)
    timestamp = Column(BigInteger, nullable=False)
    owner_id = Column(Integer, ForeignKey("users.id"))

    # Analisis AI (disimpan setelah diproses)
    sentiment_score = Column(String, nullable=True)
    key_emotions = Column(String, nullable=True) # Disimpan sebagai string JSON

    # Hubungan kembali ke pengguna
    owner = relationship("User", back_populates="journal_entries")