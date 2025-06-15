# Lokasi: ./app/db/models/journal.py
# Deskripsi: Model SQLAlchemy untuk tabel 'journalentries'.

from sqlalchemy import Column, Integer, String, Text, ForeignKey, BigInteger
from sqlalchemy.orm import relationship
from app.db.base_class import Base

class JournalEntry(Base):
    __tablename__ = "journalentries" # Nama tabel eksplisit
    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    content = Column(Text)
    mood = Column(String)
    timestamp = Column(BigInteger, nullable=False, index=True)
    owner_id = Column(Integer, ForeignKey("users.id"))

    owner = relationship("User", back_populates="journals")