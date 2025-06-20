import os
import importlib
import pytest
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

# Set required env variables
os.environ["DATABASE_URL"] = "sqlite:///./test.db"
os.environ["SECRET_KEY"] = "test"
os.environ["AI_API_URL"] = "http://test"
os.environ["AI_MODEL"] = "test-model"
os.environ["AI_PLANNER_MODEL"] = "test-model"
os.environ["AI_GENERATOR_MODEL"] = "test-model"
os.environ["AI_API_KEY"] = "initial"

from app.core import config


def test_ai_api_key_placeholder():
    os.environ["AI_API_KEY"] = "CHANGE_ME"
    with pytest.raises(ValueError):
        importlib.reload(config)


def test_ai_api_key_valid():
    os.environ["AI_API_KEY"] = "validkey"
    module = importlib.reload(config)
    assert module.Settings().AI_API_KEY == "validkey"

