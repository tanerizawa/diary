import os
os.environ.setdefault("DATABASE_URL", "sqlite:///:memory:")
os.environ.setdefault("SECRET_KEY", "test")
os.environ.setdefault("AI_API_KEY", "test")
os.environ.setdefault("AI_API_URL", "http://test")
os.environ.setdefault("AI_MODEL", "test-model")
os.environ.setdefault("CELERY_BROKER_URL", "memory://")
os.environ.setdefault("CELERY_RESULT_BACKEND", "cache+memory://")
os.environ.setdefault("CELERY_TASK_ALWAYS_EAGER", "true")

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.db import Base
from app import crud, schemas
from app.db.session import SessionLocal
from app.tasks import process_journal_sentiment, process_chat_sentiment

import pytest

engine = create_engine(
    "sqlite:///:memory:",
    connect_args={"check_same_thread": False},
    poolclass=StaticPool,
)
TestingSessionLocal = sessionmaker(bind=engine)
Base.metadata.create_all(bind=engine)

# Patch SessionLocal used in tasks
SessionLocal.configure(bind=engine)

@pytest.fixture
def db():
    session = TestingSessionLocal()
    try:
        yield session
    finally:
        session.close()


def test_process_journal_sentiment(db, monkeypatch):
    user = crud.user.create(db, obj_in=schemas.UserCreate(email="a@b.c", password="x"))
    journal = crud.journal.create_with_owner(db, obj_in=schemas.JournalCreate(title="t", content="c", mood="ok", timestamp=1), owner_id=user.id)

    async def fake_analyze(text: str):
        return {"sentiment_score": 1.0, "key_emotions": "happy"}

    monkeypatch.setattr("app.tasks.analyze_sentiment_with_ai", fake_analyze)
    from backend.app.celery_app import celery_app
    celery_app.conf.task_always_eager = True

    process_journal_sentiment.apply(args=[journal.id]).get()

    session = TestingSessionLocal()
    updated = crud.journal.get(session, id=journal.id)
    session.close()
    assert updated.sentiment_score == 1.0
    assert updated.key_emotions == "happy"


def test_process_chat_sentiment(db, monkeypatch):
    user = crud.user.create(db, obj_in=schemas.UserCreate(email="b@b.c", password="x"))
    msg = crud.chat_message.create_with_owner(db, obj_in=schemas.ChatMessageCreate(text="hi", is_user=True, timestamp=1), owner_id=user.id)

    async def fake_analyze(text: str):
        return {"sentiment_score": -0.5, "key_emotions": "sad"}

    monkeypatch.setattr("app.tasks.analyze_sentiment_with_ai", fake_analyze)
    from backend.app.celery_app import celery_app
    celery_app.conf.task_always_eager = True

    process_chat_sentiment.apply(args=[msg.id]).get()

    session = TestingSessionLocal()
    updated = crud.chat_message.get(session, id=msg.id)
    session.close()
    assert updated.sentiment_score == -0.5
    assert updated.key_emotions == "sad"
