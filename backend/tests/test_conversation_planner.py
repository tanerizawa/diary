import os
os.environ.setdefault("DATABASE_URL", "sqlite:///./test.db")
os.environ.setdefault("SECRET_KEY", "test")
os.environ.setdefault("AI_API_KEY", "test")
os.environ.setdefault("AI_API_URL", "http://test")
os.environ.setdefault("AI_MODEL", "test-model")
import sys
import asyncio
import httpx

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.services.conversation_planner import plan_conversation_strategy


def test_plan_conversation_strategy_parses_json(monkeypatch):
    class DummyClient:
        async def __aenter__(self):
            return self
        async def __aexit__(self, exc_type, exc, tb):
            pass
        async def post(self, url, headers=None, json=None, timeout=None):
            class Resp:
                def raise_for_status(self):
                    pass
                def json(self):
                    return {"choices": [{"message": {"content": '{"technique":"reflection"}'}}]}
            return Resp()

    monkeypatch.setattr("app.services.conversation_planner.httpx.AsyncClient", DummyClient)
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan is not None
    assert plan.technique == "reflection"


def test_plan_conversation_strategy_api_error(monkeypatch):
    class DummyClient:
        async def __aenter__(self):
            return self
        async def __aexit__(self, exc_type, exc, tb):
            pass
        async def post(self, url, headers=None, json=None, timeout=None):
            class Resp:
                def raise_for_status(self):
                    raise httpx.HTTPStatusError("bad", request=None, response=None)
            return Resp()

    monkeypatch.setattr("app.services.conversation_planner.httpx.AsyncClient", DummyClient)
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan is None
