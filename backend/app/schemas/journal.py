# Lokasi: ./app/schemas/journal.py

from pydantic import BaseModel, Field, ConfigDict

class JournalBase(BaseModel):
    title: str | None = Field(None, description="Title of the journal entry")
    content: str = Field(..., description="Content of the journal entry")
    mood: str = Field(..., description="Mood associated with the entry")
    timestamp: int = Field(..., description="Unix timestamp of entry creation")

class JournalCreate(JournalBase):
    pass

class JournalUpdate(JournalBase):
    pass

class Journal(JournalBase):
    id: int = Field(..., description="Unique journal identifier")
    owner_id: int = Field(..., description="Owner user identifier")

    # --- PENAMBAHAN BARU ---
    # Field untuk menampilkan hasil analisis AI di respons API
    sentiment_score: float | None = Field(
        None, description="Sentiment score from AI analysis"
    )
    key_emotions: str | None = Field(
        None, description="Key emotions extracted from the entry"
    )
    # --- AKHIR PENAMBAHAN ---

    # Config untuk kompatibilitas dengan ORM
    model_config = ConfigDict(from_attributes=True)
