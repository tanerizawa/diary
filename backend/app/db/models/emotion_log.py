from sqlalchemy import Column, Integer, String, Text, BigInteger, ForeignKey, Float
from sqlalchemy.types import JSON
from sqlalchemy.orm import relationship
from app.db.base_class import Base

class EmotionLog(Base):
    __tablename__ = "emotion_logs"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    timestamp = Column(BigInteger, nullable=False, index=True)
    detected_mood = Column(String, nullable=True)
    source_text = Column(Text)
    source_feature = Column(String)
    sentiment_score = Column(Float, nullable=True)
    key_emotions_detected = Column(JSON, nullable=True)

    user = relationship("User")
