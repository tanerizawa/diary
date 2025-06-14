# File: app/schemas.py
# Deskripsi: Mendefinisikan skema Pydantic untuk validasi data request dan response.
# Ini memastikan data yang masuk dan keluar dari API memiliki format yang benar.

from pydantic import BaseModel, ConfigDict
from typing import List, Optional

# --- Skema untuk Jurnal ---

class JournalEntryBase(BaseModel):
    title: str
    content: str
    mood: str
    timestamp: int

class JournalEntryCreate(JournalEntryBase):
    pass

# Skema untuk memperbarui entri jurnal yang ada. Saat ini sama dengan
# `JournalEntryCreate` tetapi dipisahkan untuk kejelasan API.
class JournalEntryUpdate(JournalEntryBase):
    pass

class JournalEntryResponse(JournalEntryBase):
    id: int
    owner_id: int
    sentiment_score: Optional[str] = None
    key_emotions: Optional[str] = None # Akan berisi string JSON dari list emosi

    # Menggantikan 'from_orm' yang sudah usang di Pydantic v2
    model_config = ConfigDict(from_attributes=True)

# --- Skema untuk Pengguna ---

class UserBase(BaseModel):
    email: str

class UserCreate(UserBase):
    password: str

class UserResponse(UserBase):
    id: int
    is_active: bool
    # Relasi ini akan dimuat oleh SQLAlchemy
    journal_entries: List[JournalEntryResponse] = []

    model_config = ConfigDict(from_attributes=True)

# --- Skema untuk Token ---
class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    email: Optional[str] = None