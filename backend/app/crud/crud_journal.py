# Lokasi: ./app/crud/crud_journal.py
# Deskripsi: Operasi CRUD spesifik untuk model JournalEntry.

from typing import List
from sqlalchemy.orm import Session
from app.crud.base import CRUDBase
from app.db.models.journal import JournalEntry
from app.schemas.journal import JournalCreate, JournalUpdate

class CRUDJournal(CRUDBase[JournalEntry, JournalCreate, JournalUpdate]):
    def create_with_owner(
            self, db: Session, *, obj_in: JournalCreate, owner_id: int
    ) -> JournalEntry:
        # Menggunakan Pydantic v2 `model_dump`
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
            .order_by(self.model.timestamp.desc()) # Urutkan dari yang terbaru
            .offset(skip)
            .limit(limit)
            .all()
        )

journal = CRUDJournal(JournalEntry)