from sqlalchemy import Column, Integer, Text, Boolean, BigInteger, ForeignKey, Float
from sqlalchemy.orm import relationship
from app.db.base_class import Base

class ChatMessage(Base):
    __tablename__ = "chatmessages"
    id = Column(Integer, primary_key=True, index=True)
    text = Column(Text)
    is_user = Column(Boolean, default=True)
    timestamp = Column(BigInteger, nullable=False, index=True)
    owner_id = Column(Integer, ForeignKey("users.id"))

    # Optional sentiment analysis results
    sentiment_score = Column(Float, nullable=True)
    key_emotions = Column(Text, nullable=True)

    owner = relationship("User")
