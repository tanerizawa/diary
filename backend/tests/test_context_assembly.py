import os
os.environ.setdefault("DATABASE_URL", "sqlite:///./test.db")
os.environ.setdefault("SECRET_KEY", "test")
os.environ.setdefault("AI_API_KEY", "test")
os.environ.setdefault("AI_API_URL", "http://test")
os.environ.setdefault("AI_MODEL", "test-model")
import sys
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.db import Base
from app.api import deps
from app import crud
from backend.main import app


@pytest.fixture
def client():
    engine = create_engine(
        "sqlite:///:memory:",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    TestingSessionLocal = sessionmaker(bind=engine)
    Base.metadata.create_all(bind=engine)

    def override_get_db():
        db = TestingSessionLocal()
        try:
            yield db
        finally:
            db.close()

    app.dependency_overrides[deps.get_db] = override_get_db

    from app.tasks import process_journal_sentiment, process_chat_sentiment
    process_journal_sentiment.delay = lambda *a, **k: None
    process_chat_sentiment.delay = lambda *a, **k: None

    with TestClient(app) as c:
        yield c

    app.dependency_overrides.clear()


def register_and_login(client, email="user@example.com", password="pass"):
    reg = client.post("/api/v1/users/register", json={"email": email, "password": password})
    assert reg.status_code == 200
    login = client.post("/api/v1/users/login", json={"email": email, "password": password})
    assert login.status_code == 200
    token = login.json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


from app.schemas.conversation import ConversationPlan


def test_chat_context_assembly(client, monkeypatch):
    captured = {}

    async def fake_plan(context: str, user_message: str):
        captured["context"] = context
        return ConversationPlan(technique="mirror")

    async def fake_generate(plan: ConversationPlan, user_message: str):
        return "ok"

    monkeypatch.setattr("app.api.v1.endpoints.chat.plan_conversation_strategy", fake_plan)
    monkeypatch.setattr("app.api.v1.endpoints.chat.generate_pure_response", fake_generate)
    async def fake_analyze(*args, **kwargs):
        return None
    monkeypatch.setattr("app.api.v1.endpoints.chat.analyze_sentiment_with_ai", fake_analyze)
    from app.tasks import process_chat_sentiment, process_journal_sentiment
    monkeypatch.setattr(process_chat_sentiment, "delay", lambda *a, **k: None)
    monkeypatch.setattr(process_journal_sentiment, "delay", lambda *a, **k: None)
    monkeypatch.setattr("app.services.chat_context._time_of_day", lambda: "morning")

    headers = register_and_login(client)

    client.post(
        "/api/v1/journal/",
        json={"title": "j1", "content": "I am happy", "mood": "happy", "timestamp": 1},
        headers=headers,
    )
    client.post(
        "/api/v1/journal/",
        json={"title": "j2", "content": "I am sad", "mood": "sad", "timestamp": 2},
        headers=headers,
    )

    client.post(
        "/api/v1/chat/messages",
        json={"text": "m1", "is_user": True, "timestamp": 1},
        headers=headers,
    )
    client.post(
        "/api/v1/chat/messages",
        json={"text": "m2", "is_user": True, "timestamp": 2},
        headers=headers,
    )

    resp = client.post("/api/v1/chat/", json={"message": "hello"}, headers=headers)
    assert resp.status_code == 200

    expected_prefix = (
        "Time of day: morning\n"
        "Mood frequencies: sad:1, happy:1\n"
        "Similar entries:\n"
        "m2\n"
        "m1\n"
        "I am happy"
    )
    assert captured["context"].startswith(expected_prefix)


def test_prompt_context_assembly(client, monkeypatch):
    captured = {}

    async def fake_plan(context: str, user_message: str):
        captured["context"] = context
        return ConversationPlan(technique="mirror")

    async def fake_generate(plan: ConversationPlan, user_message: str):
        return "ok"

    monkeypatch.setattr("app.api.v1.endpoints.chat.plan_conversation_strategy", fake_plan)
    monkeypatch.setattr("app.api.v1.endpoints.chat.generate_pure_response", fake_generate)
    async def fake_analyze(*args, **kwargs):
        return None
    monkeypatch.setattr("app.api.v1.endpoints.chat.analyze_sentiment_with_ai", fake_analyze)
    from app.tasks import process_chat_sentiment, process_journal_sentiment
    monkeypatch.setattr(process_chat_sentiment, "delay", lambda *a, **k: None)
    monkeypatch.setattr(process_journal_sentiment, "delay", lambda *a, **k: None)
    monkeypatch.setattr("app.services.chat_context._time_of_day", lambda: "morning")

    headers = register_and_login(client, email="prompt@example.com")

    client.post(
        "/api/v1/journal/",
        json={"title": "j1", "content": "I am happy", "mood": "happy", "timestamp": 1},
        headers=headers,
    )
    client.post(
        "/api/v1/journal/",
        json={"title": "j2", "content": "I am sad", "mood": "sad", "timestamp": 2},
        headers=headers,
    )

    client.post(
        "/api/v1/chat/messages",
        json={"text": "m1", "is_user": True, "timestamp": 1},
        headers=headers,
    )
    client.post(
        "/api/v1/chat/messages",
        json={"text": "m2", "is_user": True, "timestamp": 2},
        headers=headers,
    )

    resp = client.post("/api/v1/chat/prompt", headers=headers)
    assert resp.status_code == 200

    expected = (
        "Time of day: morning\n"
        "Mood frequencies: sad:1, happy:1\n"
        "Recent journal entries:\n"
        "I am sad\n"
        "I am happy\n"
        "Recent conversation:\n"
        "m2\n"
        "m1\n"
        "Akhiri jawaban dengan pertanyaan singkat yang bersifat probing."
    )
    assert captured["context"] == expected
