# Lokasi: ./app/schemas/journal.py

from pydantic import BaseModel

class JournalBase(BaseModel):
    title: str | None = None
    content: str
    mood: str
    timestamp: int

class JournalCreate(JournalBase):
    pass

class JournalUpdate(JournalBase):
    pass

class Journal(JournalBase):
    id: int
    owner_id: int

    # --- PENAMBAHAN BARU ---
    # Field untuk menampilkan hasil analisis AI di respons API
    sentiment_score: float | None = None
    key_emotions: str | None = None
    # --- AKHIR PENAMBAHAN ---

    # Config untuk kompatibilitas dengan ORM
    class Config:
        from_attributes = True
