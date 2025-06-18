from typing import List
from sqlalchemy.orm import Session
from app.crud.base import CRUDBase
from app.db.models.chat import ChatMessage
from app.schemas.chat_message import ChatMessageCreate, ChatMessageUpdate
from app.db.session import SessionLocal
from app.services.sentiment_analyzer import analyze_sentiment_with_ai

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

    async def process_and_update_sentiment(self, *, chat_id: int):
        """Analyze sentiment for a chat message and store the result."""
        db = SessionLocal()
        try:
            db_obj = self.get(db=db, id=chat_id)
            if db_obj:
                analysis_result = await analyze_sentiment_with_ai(db_obj.text)
                if analysis_result:
                    db_obj.sentiment_score = analysis_result.get("sentiment_score")
                    db_obj.key_emotions = analysis_result.get("key_emotions")
                    db.add(db_obj)
                    db.commit()
                    db.refresh(db_obj)
        finally:
            db.close()

chat_message = CRUDChatMessage(ChatMessage)
