import os

os.environ.setdefault("DATABASE_URL", "sqlite:///./test.db")
os.environ.setdefault("SECRET_KEY", "test")
os.environ.setdefault("AI_API_KEY", "test")
os.environ.setdefault("AI_API_URL", "http://test")
os.environ.setdefault("AI_MODEL", "test-model")
os.environ.setdefault("AI_PLANNER_MODEL", "test-model")
os.environ.setdefault("AI_GENERATOR_MODEL", "test-model")
import sys
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.db import Base
from app.api import deps
from backend.main import app
from app.schemas.conversation import ConversationPlan, CommunicationTechnique


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
    original_session_local = deps.SessionLocal
    deps.SessionLocal = TestingSessionLocal

    from app.tasks import process_chat_sentiment

    process_chat_sentiment.delay = lambda *a, **k: None

    with TestClient(app) as c:
        yield c

    app.dependency_overrides.clear()
    deps.SessionLocal = original_session_local


def register_and_login(client, email="ws@example.com", password="pass"):
    reg = client.post(
        "/api/v1/users/register", json={"email": email, "password": password}
    )
    assert reg.status_code == 200
    login = client.post(
        "/api/v1/users/login", json={"email": email, "password": password}
    )
    assert login.status_code == 200
    token = login.json()["access_token"]
    return token


def test_websocket_chat(client, monkeypatch):
    token = register_and_login(client)

    async def fake_plan(
        context: str, user_message: str, previous_ai_text: str | None = None
    ):
        return ConversationPlan(technique=CommunicationTechnique.REFLECTING)

    async def fake_generate(plan: ConversationPlan, user_message: str):
        return "pong"

    async def fake_analysis(text: str):
        return {"issue_type": "stress", "technique": "breathing", "tone": "tense"}

    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.plan_conversation_strategy", fake_plan
    )
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.generate_pure_response", fake_generate
    )
    monkeypatch.setattr("app.api.v1.endpoints.chat.analyze_message", fake_analysis)
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.analyze_sentiment_with_ai", lambda *a, **k: None
    )

    with client.websocket_connect(f"/api/v1/chat/ws?token={token}") as ws:
        ws.send_text("hello")
        data = ws.receive_json()
        assert data["text_response"] == "pong"
