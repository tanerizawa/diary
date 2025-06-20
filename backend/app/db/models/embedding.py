from sqlalchemy import Column, Integer, String, ForeignKey
from sqlalchemy.types import JSON
from sqlalchemy.orm import relationship
from app.db.base_class import Base

class SemanticEmbedding(Base):
    """Vector embedding linked to either a chat message or journal entry."""
    __tablename__ = "semantic_embeddings"

    id = Column(Integer, primary_key=True, index=True)
    owner_id = Column(Integer, ForeignKey("users.id"), index=True)
    source_type = Column(String, nullable=False)  # "chat" or "journal"
    source_id = Column(Integer, nullable=False)
    embedding = Column(JSON, nullable=False)

    owner = relationship("User")
