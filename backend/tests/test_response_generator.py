import os
os.environ.setdefault("DATABASE_URL", "sqlite:///./test.db")
os.environ.setdefault("SECRET_KEY", "test")
os.environ.setdefault("AI_API_KEY", "test")
os.environ.setdefault("AI_API_URL", "http://test")
os.environ.setdefault("AI_MODEL", "test-model")
os.environ.setdefault("AI_PLANNER_MODEL", "test-model")
os.environ.setdefault("AI_GENERATOR_MODEL", "test-model")
import sys
import asyncio
import httpx

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.schemas.conversation import ConversationPlan, CommunicationTechnique
from app.services.response_generator import generate_pure_response


def test_generate_pure_response_prompt(monkeypatch):
    captured = {}

    class DummyClient:
        async def __aenter__(self):
            return self

        async def __aexit__(self, exc_type, exc, tb):
            pass

        async def post(self, url, headers=None, json=None, timeout=None):
            captured['json'] = json

            class Resp:
                def raise_for_status(self):
                    pass

                def json(self):
                    return {"choices": [{"message": {"content": "reply"}}]}

            return Resp()

    monkeypatch.setattr("app.services.response_generator.httpx.AsyncClient", DummyClient)
    plan = ConversationPlan(technique=CommunicationTechnique.REFLECTING)
    result = asyncio.run(generate_pure_response(plan, "hi"))
    assert result == "reply"
    assert "Reflecting" in captured['json']['messages'][0]['content']
    assert 'response_format' not in captured['json']
