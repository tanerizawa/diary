# File: app/crud/crud_journal.py
# Deskripsi: Berisi fungsi-fungsi CRUD untuk model JournalEntry,
# termasuk logika bisnis untuk analisis AI.

import httpx
import json
from sqlalchemy.orm import Session
from fastapi import HTTPException, status

from app import models, schemas
from app.core.config import settings

async def _analyze_text_with_ai(text: str) -> str | None:
    """
    Fungsi internal untuk mengirim teks ke OpenRouter.ai untuk dianalisis.
    Melemparkan HTTPException jika terjadi kegagalan.
    """
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                "https://openrouter.ai/api/v1/chat/completions",
                headers={
                    "Authorization": f"Bearer {settings.OPENROUTER_API_KEY}",
                },
                json={
                    "model": "mistralai/mistral-7b-instruct",
                    "messages": [
                        {"role": "system", "content": "Analyze the sentiment of the following text and identify key emotions. Respond ONLY in a valid JSON format with keys 'sentiment_score' (string: 'positive', 'negative', 'neutral') and 'key_emotions' (a list of strings)."},
                        {"role": "user", "content": text}
                    ]
                }
            )
            response.raise_for_status() # Melemparkan error untuk status 4xx atau 5xx
            ai_result = response.json()
            return ai_result['choices'][0]['message']['content']
    except httpx.HTTPStatusError as e:
        # Jika API eksternal gagal, lemparkan error yang informatif
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"AI service failed: {e.response.text}"
        )
    except Exception as e:
        # Untuk error tak terduga lainnya
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"An unexpected error occurred during AI analysis: {str(e)}"
        )


async def create_journal_entry(db: Session, *, entry_in: schemas.JournalEntryCreate, owner_id: int) -> models.JournalEntry:
    """
    Membuat entri jurnal, menganalisis konten via AI, dan menyimpannya ke database.
    """
    db_entry = models.JournalEntry(**entry_in.dict(), owner_id=owner_id)

    # Panggil AI untuk analisis dan tangani hasilnya
    analysis_result_str = await _analyze_text_with_ai(entry_in.content)
    if analysis_result_str:
        try:
            analysis_data = json.loads(analysis_result_str)
            db_entry.sentiment_score = analysis_data.get("sentiment_score")
            # Simpan list dari 'key_emotions' sebagai string JSON di database
            db_entry.key_emotions = json.dumps(analysis_data.get("key_emotions", []))
        except json.JSONDecodeError:
            # Jika AI tidak mengembalikan JSON yang valid
            db_entry.sentiment_score = "invalid_ai_response"
            db_entry.key_emotions = "[]"

    db.add(db_entry)
    db.commit()
    db.refresh(db_entry)
    return db_entry


def get_user_entries(db: Session, *, owner_id: int, skip: int = 0, limit: int = 100) -> list[models.JournalEntry]:
    """Membaca daftar entri jurnal milik pengguna dari database."""
    return db.query(models.JournalEntry).filter(models.JournalEntry.owner_id == owner_id).offset(skip).limit(limit).all()