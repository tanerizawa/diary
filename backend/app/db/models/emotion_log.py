from sqlalchemy import Column, Integer, String, Text, BigInteger, ForeignKey
from sqlalchemy.orm import relationship
from app.db.base_class import Base

class EmotionLog(Base):
    __tablename__ = "emotionlogs"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    timestamp = Column(BigInteger, nullable=False, index=True)
    detected_mood = Column(String, nullable=True)
    source_text = Column(Text)
    source_feature = Column(String)

    user = relationship("User")
