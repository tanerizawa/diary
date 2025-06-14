# Lokasi: ./app/db/models/user.py
# Deskripsi: Model SQLAlchemy untuk tabel 'users'.

from sqlalchemy import Column, Integer, String, Boolean
from sqlalchemy.orm import relationship
from app.db.base_class import Base

class User(Base):
    __tablename__ = "users" # Nama tabel eksplisit
    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_active = Column(Boolean(), default=True)

    journals = relationship("JournalEntry", back_populates="owner", cascade="all, delete-orphan")
