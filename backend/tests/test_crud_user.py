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
from app.crud.crud_user import user


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


def test_user_crud_flow(db_session):
    # create
    new_user = user.create(db_session, obj_in=UserCreate(email="a@b.com", password="pass"))
    assert new_user.id is not None
    assert new_user.email == "a@b.com"
    assert new_user.hashed_password != "pass"
    assert new_user.relationship_level == 0
    assert new_user.mbti_type is None

    # update
    updated = user.update(db_session, db_obj=new_user, obj_in={"is_active": False})
    assert updated.is_active is False

    # update mbti
    updated = user.update(db_session, db_obj=new_user, obj_in={"mbti_type": "INTJ"})
    assert updated.mbti_type == "INTJ"

    # get by email
    got = user.get_by_email(db_session, email="a@b.com")
    assert got.id == new_user.id
    assert got.mbti_type == "INTJ"

    # remove
    user.remove(db_session, id=new_user.id)
    assert user.get(db_session, id=new_user.id) is None
