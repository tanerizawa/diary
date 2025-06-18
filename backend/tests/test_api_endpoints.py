import os
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

    async def noop(*args, **kwargs):
        return None

    crud.journal.process_and_update_sentiment = noop

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


def test_user_registration_login_delete(client):
    headers = register_and_login(client)
    resp = client.delete("/api/v1/users/me", headers=headers)
    assert resp.status_code == 200


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
