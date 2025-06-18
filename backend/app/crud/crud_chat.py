from typing import List
from sqlalchemy.orm import Session
from app.crud.base import CRUDBase
from app.db.models.chat import ChatMessage
from app.schemas.chat_message import ChatMessageCreate, ChatMessageUpdate

class CRUDChatMessage(CRUDBase[ChatMessage, ChatMessageCreate, ChatMessageUpdate]):
    def create_with_owner(self, db: Session, *, obj_in: ChatMessageCreate, owner_id: int) -> ChatMessage:
        obj_in_data = obj_in.model_dump()
        db_obj = ChatMessage(**obj_in_data, owner_id=owner_id)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
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

chat_message = CRUDChatMessage(ChatMessage)
