from sqlalchemy.orm import Session
from app.db.models.embedding import SemanticEmbedding

class CRUDEmbedding:
    """CRUD operations for SemanticEmbedding."""

    def create(
        self,
        db: Session,
        *,
        owner_id: int,
        source_type: str,
        source_id: int,
        embedding: list[float],
    ) -> SemanticEmbedding:
        db_obj = SemanticEmbedding(
            owner_id=owner_id,
            source_type=source_type,
            source_id=source_id,
            embedding=embedding,
        )
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def get_all_by_owner(self, db: Session, *, owner_id: int) -> list[SemanticEmbedding]:
        return db.query(SemanticEmbedding).filter(SemanticEmbedding.owner_id == owner_id).all()

    def get_by_source(
        self, db: Session, *, source_type: str, source_id: int
    ) -> SemanticEmbedding | None:
        return (
            db.query(SemanticEmbedding)
            .filter(
                SemanticEmbedding.source_type == source_type,
                SemanticEmbedding.source_id == source_id,
            )
            .first()
        )

embedding = CRUDEmbedding()
