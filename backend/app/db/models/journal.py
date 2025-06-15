# Lokasi: ./app/db/models/journal.py

from sqlalchemy import Column, Integer, String, Text, ForeignKey, BigInteger, Float # PERBAIKAN: Tambah 'Float'
from sqlalchemy.orm import relationship
from app.db.base_class import Base

class JournalEntry(Base):
    __tablename__ = "journalentries"
    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    content = Column(Text)
    mood = Column(String)
    timestamp = Column(BigInteger, nullable=False, index=True)
    owner_id = Column(Integer, ForeignKey("users.id"))

    # --- PENAMBAHAN BARU ---
    # Kolom untuk menyimpan hasil analisis AI, bisa null
    sentiment_score = Column(Float, nullable=True)
    key_emotions = Column(String, nullable=True)
    # --- AKHIR PENAMBAHAN ---

    owner = relationship("User", back_populates="journals")