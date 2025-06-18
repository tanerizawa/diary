# Lokasi: ./app/crud/crud_journal.py

from typing import List
from sqlalchemy.orm import Session
from app.db.session import SessionLocal
from app.crud.base import CRUDBase
from app.db.models.journal import JournalEntry
from app.schemas.journal import JournalCreate, JournalUpdate
from app.services.sentiment_analyzer import (
    analyze_sentiment_with_ai,
)  # PENAMBAHAN IMPORT


class CRUDJournal(CRUDBase[JournalEntry, JournalCreate, JournalUpdate]):
    def create_with_owner(
        self, db: Session, *, obj_in: JournalCreate, owner_id: int
    ) -> JournalEntry:
        obj_in_data = obj_in.model_dump()
        db_obj = self.model(**obj_in_data, owner_id=owner_id)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def get_multi_by_owner(
        self, db: Session, *, owner_id: int, skip: int = 0, limit: int = 100
    ) -> List[JournalEntry]:
        return (
            db.query(self.model)
            .filter(JournalEntry.owner_id == owner_id)
            .order_by(self.model.timestamp.desc())
            .offset(skip)
            .limit(limit)
            .all()
        )

    # --- PENAMBAHAN BARU ---
    async def process_and_update_sentiment(self, *, journal_id: int):
        """
        Fungsi yang dijalankan di background untuk mengambil jurnal,
        menganalisis sentimennya, dan menyimpan hasilnya.
        """
        db = SessionLocal()
        try:
            db_obj = self.get(db=db, id=journal_id)
            if db_obj:
                analysis_result = await analyze_sentiment_with_ai(db_obj.content)
                if analysis_result:
                    db_obj.sentiment_score = analysis_result.get("sentiment_score")
                    db_obj.key_emotions = analysis_result.get("key_emotions")
                    db.add(db_obj)
                    db.commit()
                    db.refresh(db_obj)
        finally:
            db.close()

    # --- AKHIR PENAMBAHAN ---


journal = CRUDJournal(JournalEntry)
