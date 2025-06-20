from typing import List
from sqlalchemy.orm import Session
from app.crud.base import CRUDBase
from app.db.models.chat import ChatMessage
from app.schemas.chat_message import ChatMessageCreate, ChatMessageUpdate
from .crud_user import user as crud_user
from .crud_embedding import embedding as crud_embedding
from app.services.embedding import generate_embedding

class CRUDChatMessage(CRUDBase[ChatMessage, ChatMessageCreate, ChatMessageUpdate]):
    def create_with_owner(self, db: Session, *, obj_in: ChatMessageCreate, owner_id: int) -> ChatMessage:
        obj_in_data = obj_in.model_dump()
        db_obj = ChatMessage(**obj_in_data, owner_id=owner_id)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        if db_obj.is_user:
            user_obj = crud_user.get(db, id=owner_id)
            if user_obj:
                crud_user.increment_relationship_level(db, db_obj=user_obj)
        # Store embedding for semantic search
        vector = generate_embedding(db_obj.text)
        crud_embedding.create(
            db,
            owner_id=owner_id,
            source_type="chat",
            source_id=db_obj.id,
            embedding=vector,
        )
        return db_obj

    def get_multi_by_owner(self, db: Session, *, owner_id: int, skip: int = 0, limit: int = 100) -> List[ChatMessage]:
        return (
            db.query(self.model)
            .filter(ChatMessage.owner_id == owner_id)
            .order_by(self.model.timestamp.asc())
            .offset(skip)
            .limit(limit)
            .all()
        )

    def get_last_user_messages(self, db: Session, *, owner_id: int, limit: int) -> List[ChatMessage]:
        """Return the latest user messages for a given owner."""
        return (
            db.query(self.model)
            .filter(ChatMessage.owner_id == owner_id, ChatMessage.is_user == True)
            .order_by(self.model.timestamp.desc())
            .limit(limit)
            .all()
        )

    def get_recent_messages(self, db: Session, *, owner_id: int, limit: int) -> List[ChatMessage]:
        """Return the most recent messages regardless of sender."""
        return (
            db.query(self.model)
            .filter(ChatMessage.owner_id == owner_id)
            .order_by(self.model.timestamp.desc())
            .limit(limit)
            .all()
        )

    def remove_multi(self, db: Session, *, ids: List[int], owner_id: int) -> int:
        """Delete multiple messages belonging to the given owner.

        Returns the number of rows deleted."""
        result = (
            db.query(self.model)
            .filter(ChatMessage.owner_id == owner_id, ChatMessage.id.in_(ids))
            .delete(synchronize_session=False)
        )
        db.commit()
        return result


chat_message = CRUDChatMessage(ChatMessage)
