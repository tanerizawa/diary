# Lokasi: ./app/crud/crud_journal.py

from typing import List
from sqlalchemy.orm import Session
from app.crud.base import CRUDBase
from app.db.models.journal import JournalEntry
from app.schemas.journal import JournalCreate, JournalUpdate
from .crud_user import user as crud_user
from .crud_embedding import embedding as crud_embedding
from app.services.embedding import generate_embedding


class CRUDJournal(CRUDBase[JournalEntry, JournalCreate, JournalUpdate]):
    def create_with_owner(
        self, db: Session, *, obj_in: JournalCreate, owner_id: int
    ) -> JournalEntry:
        obj_in_data = obj_in.model_dump()
        db_obj = self.model(**obj_in_data, owner_id=owner_id)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        owner = crud_user.get(db, id=owner_id)
        if owner:
            crud_user.increment_relationship_level(db, db_obj=owner)
        # Store embedding for semantic search
        vector = generate_embedding(db_obj.content)
        crud_embedding.create(
            db,
            owner_id=owner_id,
            source_type="journal",
            source_id=db_obj.id,
            embedding=vector,
        )
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

journal = CRUDJournal(JournalEntry)
