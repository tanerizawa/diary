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
from app import crud
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

    from app.tasks import process_journal_sentiment, process_chat_sentiment

    process_journal_sentiment.delay = lambda *a, **k: None
    process_chat_sentiment.delay = lambda *a, **k: None

    with TestClient(app) as c:
        yield c

    app.dependency_overrides.clear()


def register_and_login(client, email="user@example.com", password="pass"):
    reg = client.post(
        "/api/v1/users/register", json={"email": email, "password": password}
    )
    assert reg.status_code == 200
    login = client.post(
        "/api/v1/users/login", json={"email": email, "password": password}
    )
    assert login.status_code == 200
    token = login.json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


def test_user_registration_login_delete(client):
    headers = register_and_login(client)
    resp = client.delete("/api/v1/users/me", headers=headers)
    assert resp.status_code == 200


def test_mbti_update_endpoint(client):
    headers = register_and_login(client, email="mbti@example.com")

    resp = client.put(
        "/api/v1/users/me/mbti",
        json={"mbti_type": "ENTP"},
        headers=headers,
    )
    assert resp.status_code == 200
    assert resp.json()["mbti_type"] == "ENTP"

    check = client.get("/api/v1/users/me", headers=headers)
    assert check.status_code == 200
    assert check.json()["mbti_type"] == "ENTP"


def test_journal_crud_endpoints(client):
    headers = register_and_login(client)

    create_resp = client.post(
        "/api/v1/journal/",
        json={"title": "t", "content": "c", "mood": "ok", "timestamp": 1},
        headers=headers,
    )
    assert create_resp.status_code == 201
    journal_id = create_resp.json()["id"]

    list_resp = client.get("/api/v1/journal/", headers=headers)
    assert list_resp.status_code == 200
    assert len(list_resp.json()) == 1

    update_resp = client.put(
        f"/api/v1/journal/{journal_id}",
        json={"title": "t2", "content": "c2", "mood": "bad", "timestamp": 2},
        headers=headers,
    )
    assert update_resp.status_code == 200
    assert update_resp.json()["title"] == "t2"

    delete_resp = client.delete(f"/api/v1/journal/{journal_id}", headers=headers)
    assert delete_resp.status_code == 200


def test_feed_endpoint(client):
    headers = register_and_login(client, email="feed@example.com")
    resp = client.get("/api/v1/feed", headers=headers)
    assert resp.status_code == 200
    assert isinstance(resp.json(), list)


def test_chat_sentiment_response(client, monkeypatch):
    headers = register_and_login(client, email="chat@example.com")

    captured = {}

    async def fake_plan(
        context: str, user_message: str, previous_ai_text: str | None = None
    ):
        captured["ctx"] = context
        return ConversationPlan(technique=CommunicationTechnique.REFLECTING)

    async def fake_generate(
        plan: ConversationPlan, user_message: str, context: str, persona_trait: str
    ):
        captured["trait"] = persona_trait
        return "hi"

    async def fake_analysis(text: str):
        captured["analysis"] = {
            "issue_type": "stress",
            "technique": "breathing",
            "tone": "tense",
        }
        return captured["analysis"]

    async def fake_sentiment(text: str):
        return {"sentiment_score": 0.5, "key_emotions": "happy"}

    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.plan_conversation_strategy", fake_plan
    )
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.generate_pure_response", fake_generate
    )
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.analyze_sentiment_with_ai", fake_sentiment
    )
    monkeypatch.setattr("app.api.v1.endpoints.chat.analyze_message", fake_analysis)
    from app.tasks import process_chat_sentiment

    monkeypatch.setattr(process_chat_sentiment, "delay", lambda *a, **k: None)

    resp = client.post("/api/v1/chat/", json={"message": "hello"}, headers=headers)
    assert resp.status_code == 200
    data = resp.json()
    assert data["message_id"] > 0
    assert data["ai_message_id"] > 0
    assert data["text_response"] == "hi"
    assert (
        captured["trait"]
        == "You are still getting to know the user, so maintain a supportive but slightly formal tone."
    )
    assert captured["analysis"] == {
        "issue_type": "stress",
        "technique": "breathing",
        "tone": "tense",
    }

    logs_resp = client.get("/api/v1/emotion/", headers=headers)
    assert logs_resp.status_code == 200
    logs = logs_resp.json()
    assert len(logs) == 1
    assert logs[0]["detected_mood"] == "\U0001f610"
    assert logs[0]["sentiment_score"] is None
    assert logs[0]["key_emotions_detected"] is None


def test_chat_analysis_failure(client, monkeypatch):
    headers = register_and_login(client, email="fail@example.com")

    async def fake_plan(
        context: str, user_message: str, previous_ai_text: str | None = None
    ):
        return ConversationPlan(technique=CommunicationTechnique.REFLECTING)

    async def fake_generate(
        plan: ConversationPlan, user_message: str, context: str, persona_trait: str
    ):
        return "ok"

    async def fail_analysis(text: str):
        return None

    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.plan_conversation_strategy", fake_plan
    )
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.generate_pure_response", fake_generate
    )
    monkeypatch.setattr("app.api.v1.endpoints.chat.analyze_message", fail_analysis)
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.analyze_sentiment_with_ai", lambda *a, **k: None
    )
    from app.tasks import process_chat_sentiment

    monkeypatch.setattr(process_chat_sentiment, "delay", lambda *a, **k: None)

    resp = client.post("/api/v1/chat/", json={"message": "hi"}, headers=headers)
    assert resp.status_code == 200
    data = resp.json()
    assert "issue_type" not in data
    assert "recommended_technique" not in data
    assert "tone" not in data


def test_message_post_handler(client, monkeypatch):
    headers = register_and_login(client, email="msg@example.com")

    async def fake_plan(
        context: str, user_message: str, previous_ai_text: str | None = None
    ):
        return ConversationPlan(technique=CommunicationTechnique.REFLECTING)

    captured = {}

    async def fake_generate(
        plan: ConversationPlan, user_message: str, context: str, persona_trait: str
    ):
        captured["trait"] = persona_trait
        return "reply"

    async def fake_sentiment(text: str):
        return {"sentiment_score": 0.2, "key_emotions": "calm"}

    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.plan_conversation_strategy", fake_plan
    )
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.generate_pure_response", fake_generate
    )
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.analyze_sentiment_with_ai", fake_sentiment
    )

    resp = client.post(
        "/api/v1/chat/messages",
        json={"text": "hi", "is_user": True, "timestamp": 1},
        headers=headers,
    )
    assert resp.status_code == 200
    data = resp.json()
    assert data["message_id"] > 0
    assert data["ai_message_id"] is None
    assert data["text_response"] == "reply"
    assert (
        captured["trait"]
        == "You are still getting to know the user, so maintain a supportive but slightly formal tone."
    )

    logs_resp = client.get("/api/v1/emotion/", headers=headers)
    assert logs_resp.status_code == 200
    logs = logs_resp.json()
    assert len(logs) == 1
    assert logs[0]["sentiment_score"] == 0.2
    assert logs[0]["key_emotions_detected"] == ["calm"]


def test_delete_messages_endpoint(client, monkeypatch):
    headers = register_and_login(client, email="delmsg@example.com")

    async def fake_plan(
        context: str, user_message: str, previous_ai_text: str | None = None
    ):
        return ConversationPlan(technique=CommunicationTechnique.REFLECTING)

    async def fake_generate(
        plan: ConversationPlan, user_message: str, context: str, persona_trait: str
    ):
        return "ok"

    async def fake_sentiment(text: str):
        return None

    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.plan_conversation_strategy", fake_plan
    )
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.generate_pure_response", fake_generate
    )
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.analyze_sentiment_with_ai", fake_sentiment
    )

    for i in range(3):
        client.post(
            "/api/v1/chat/messages",
            json={"text": f"msg{i}", "is_user": True, "timestamp": i},
            headers=headers,
        )

    resp = client.get("/api/v1/chat/messages", headers=headers)
    ids = [m["id"] for m in resp.json()]
    del_resp = client.request(
        "DELETE", "/api/v1/chat/messages", json={"ids": ids[:2]}, headers=headers
    )
    assert del_resp.status_code == 200
    assert del_resp.json() == 2

    remaining = client.get("/api/v1/chat/messages", headers=headers).json()
    assert len(remaining) == 1
    assert remaining[0]["id"] == ids[2]


def test_prompt_endpoint_rate_limit(client, monkeypatch):
    headers = register_and_login(client, email="prompt@example.com")

    captured_trait = [None]

    async def fake_plan(
        context: str, user_message: str, previous_ai_text: str | None = None
    ):
        return ConversationPlan(technique=CommunicationTechnique.REFLECTING)

    async def fake_generate(
        plan: ConversationPlan, user_message: str, context: str, persona_trait: str
    ):
        captured_trait[0] = persona_trait
        return "hey?"

    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.plan_conversation_strategy", fake_plan
    )
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.generate_pure_response", fake_generate
    )

    resp = client.post("/api/v1/chat/prompt", headers=headers)
    assert resp.status_code == 200
    data = resp.json()
    assert data["text_response"] == "hey?"
    assert data["message_id"] > 0
    assert data["ai_message_id"] == data["message_id"]
    assert (
        captured_trait[0]
        == "You are still getting to know the user, so maintain a supportive but slightly formal tone."
    )

    second = client.post("/api/v1/chat/prompt", headers=headers)
    assert second.status_code == 429


def test_relationship_level_prompt_variation(client, monkeypatch):
    headers = register_and_login(client, email="rel@example.com")
    prompts = []

    async def fake_plan(
        context: str, user_message: str, previous_ai_text: str | None = None
    ):
        prompts.append(context)
        return ConversationPlan(technique=CommunicationTechnique.REFLECTING)

    async def fake_generate(
        plan: ConversationPlan, user_message: str, context: str, persona_trait: str
    ):
        return "ok"

    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.plan_conversation_strategy", fake_plan
    )
    monkeypatch.setattr(
        "app.api.v1.endpoints.chat.generate_pure_response", fake_generate
    )

    async def noop(*args, **kwargs):
        return None

    monkeypatch.setattr("app.api.v1.endpoints.chat.analyze_sentiment_with_ai", noop)
    from app.tasks import process_chat_sentiment, process_journal_sentiment

    monkeypatch.setattr(process_chat_sentiment, "delay", lambda *a, **k: None)
    monkeypatch.setattr(process_journal_sentiment, "delay", lambda *a, **k: None)

    client.post(
        "/api/v1/chat/messages",
        json={"text": "hi", "is_user": True, "timestamp": 1},
        headers=headers,
    )
    assert len(prompts) == 1

    for i in range(11):
        client.post(
            "/api/v1/journal/",
            json={"title": "t", "content": "c", "mood": "ok", "timestamp": i},
            headers=headers,
        )

    prompts.clear()
    client.post(
        "/api/v1/chat/messages",
        json={"text": "hi", "is_user": True, "timestamp": 2},
        headers=headers,
    )
    assert len(prompts) == 1

    for i in range(20):
        client.post(
            "/api/v1/journal/",
            json={"title": "t", "content": "c", "mood": "ok", "timestamp": i + 20},
            headers=headers,
        )

    prompts.clear()
    client.post(
        "/api/v1/chat/messages",
        json={"text": "hi", "is_user": True, "timestamp": 3},
        headers=headers,
    )
    assert len(prompts) == 1
