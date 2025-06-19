from typing import List
from sqlalchemy.orm import Session
from app.crud.base import CRUDBase
from app.db.models.emotion_log import EmotionLog
from app.schemas.emotion_log import EmotionLogCreate, EmotionLogUpdate

class CRUDEmotionLog(CRUDBase[EmotionLog, EmotionLogCreate, EmotionLogUpdate]):
    def create_with_owner(self, db: Session, *, obj_in: EmotionLogCreate, owner_id: int) -> EmotionLog:
        obj_in_data = obj_in.model_dump()
        db_obj = EmotionLog(**obj_in_data, user_id=owner_id)
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def get_multi_by_owner(self, db: Session, *, owner_id: int, skip: int = 0, limit: int = 100) -> List[EmotionLog]:
        return (
            db.query(self.model)
            .filter(EmotionLog.user_id == owner_id)
            .order_by(EmotionLog.timestamp.desc())
            .offset(skip)
            .limit(limit)
            .all()
        )

emotion_log = CRUDEmotionLog(EmotionLog)
