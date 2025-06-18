from sqlalchemy import Column, Integer, Text, Boolean, BigInteger, ForeignKey
from sqlalchemy.orm import relationship
from app.db.base_class import Base

class ChatMessage(Base):
    __tablename__ = "chatmessages"
    id = Column(Integer, primary_key=True, index=True)
    text = Column(Text)
    is_user = Column(Boolean, default=True)
    timestamp = Column(BigInteger, nullable=False, index=True)
    owner_id = Column(Integer, ForeignKey("users.id"))

    owner = relationship("User")
