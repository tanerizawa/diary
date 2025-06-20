import os
os.environ.setdefault("DATABASE_URL", "sqlite:///:memory:")
os.environ.setdefault("SECRET_KEY", "test")
os.environ.setdefault("AI_API_KEY", "test")
os.environ.setdefault("AI_API_URL", "http://test")
os.environ.setdefault("AI_MODEL", "test-model")

import sys
import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.db import Base
from app import crud
from app.schemas.user import UserCreate
from app.schemas.journal import JournalCreate
from app.schemas.chat_message import ChatMessageCreate
from app.services.embedding import find_similar_entries


@pytest.fixture
def db_session():
    engine = create_engine(
        "sqlite:///:memory:",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    TestingSessionLocal = sessionmaker(bind=engine)
    Base.metadata.create_all(bind=engine)
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()


def test_embedding_created(db_session):
    user = crud.user.create(db_session, obj_in=UserCreate(email="e@x.com", password="x"))
    msg = crud.chat_message.create_with_owner(
        db_session,
        obj_in=ChatMessageCreate(text="hello", is_user=True, timestamp=1),
        owner_id=user.id,
    )
    journal = crud.journal.create_with_owner(
        db_session,
        obj_in=JournalCreate(title="t", content="hello world", mood="ok", timestamp=1),
        owner_id=user.id,
    )
    embeddings = crud.embedding.get_all_by_owner(db_session, owner_id=user.id)
    assert len(embeddings) == 2
    src_types = sorted(e.source_type for e in embeddings)
    assert src_types == ["chat", "journal"]


def test_find_similar_entries(db_session):
    user = crud.user.create(db_session, obj_in=UserCreate(email="e2@x.com", password="x"))
    crud.chat_message.create_with_owner(
        db_session,
        obj_in=ChatMessageCreate(text="hello there", is_user=True, timestamp=1),
        owner_id=user.id,
    )
    crud.journal.create_with_owner(
        db_session,
        obj_in=JournalCreate(title="t", content="random note", mood="ok", timestamp=1),
        owner_id=user.id,
    )

    results = find_similar_entries(db_session, user, "hello there")
    assert results and results[0].startswith("hello")
