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
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.db import Base
from app.schemas.user import UserCreate
from app.schemas.journal import JournalCreate, JournalUpdate
from app.crud.crud_user import user
from app.crud.crud_journal import journal


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


def test_journal_crud_flow(db_session):
    owner = user.create(db_session, obj_in=UserCreate(email="user@example.com", password="pass"))

    entry = journal.create_with_owner(
        db_session,
        obj_in=JournalCreate(title="t", content="c", mood="happy", timestamp=1),
        owner_id=owner.id,
    )
    assert entry.id is not None
    assert entry.owner_id == owner.id
    assert user.get(db_session, id=owner.id).relationship_level == 1

    all_journals = journal.get_multi_by_owner(db_session, owner_id=owner.id)
    assert len(all_journals) == 1

    updated = journal.update(db_session, db_obj=entry, obj_in=JournalUpdate(title="t2", content="c2", mood="sad", timestamp=2))
    assert updated.title == "t2"

    journal.remove(db_session, id=entry.id)
    assert journal.get(db_session, id=entry.id) is None
