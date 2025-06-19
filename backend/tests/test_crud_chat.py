import os
os.environ.setdefault("DATABASE_URL", "sqlite:///./test.db")
os.environ.setdefault("SECRET_KEY", "test")
os.environ.setdefault("AI_API_KEY", "test")
os.environ.setdefault("AI_API_URL", "http://test")
os.environ.setdefault("AI_MODEL", "test-model")
import sys
import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.db import Base
from app.db.models.chat import ChatMessage
from app.schemas.chat_message import ChatMessageCreate
from app.crud.crud_chat import chat_message


@pytest.fixture
def db_session():
    engine = create_engine("sqlite:///:memory:", connect_args={'check_same_thread': False})
    TestingSessionLocal = sessionmaker(bind=engine)
    Base.metadata.create_all(bind=engine)
    db = TestingSessionLocal()
    try:
        yield db
    finally:
        db.close()

def test_get_last_user_messages(db_session):
    messages = [
        ChatMessage(text="u1", is_user=True, timestamp=1, owner_id=1),
        ChatMessage(text="a1", is_user=False, timestamp=2, owner_id=1),
        ChatMessage(text="u2", is_user=True, timestamp=3, owner_id=1),
        ChatMessage(text="u3", is_user=True, timestamp=4, owner_id=1),
        ChatMessage(text="u4", is_user=True, timestamp=5, owner_id=2),
    ]
    for m in messages:
        db_session.add(m)
    db_session.commit()

    results = chat_message.get_last_user_messages(db_session, owner_id=1, limit=2)
    assert [m.text for m in results] == ["u3", "u2"]


def test_chat_message_crud(db_session):
    msg_in = ChatMessageCreate(text="hi", is_user=True, timestamp=1)
    created = chat_message.create_with_owner(db_session, obj_in=msg_in, owner_id=1)
    assert created.id is not None

    updated = chat_message.update(db_session, db_obj=created, obj_in={"text": "bye"})
    assert updated.text == "bye"

    chat_message.remove(db_session, id=created.id)
    assert chat_message.get(db_session, id=created.id) is None


