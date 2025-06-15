# Lokasi: ./app/schemas/journal.py
# Deskripsi: Skema Pydantic untuk validasi data request/response jurnal.

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

    # Config untuk kompatibilitas dengan ORM
    class Config:
        from_attributes = True
