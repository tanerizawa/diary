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

from app.services.conversation_planner import plan_conversation_strategy
from app.schemas.conversation import CommunicationTechnique


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
                    return {
                        "choices": [
                            {
                                "message": {
                                    "content": '{"reasoning":"ok","technique":"reflection"}'
                                }
                            }
                        ]
                    }

            return Resp()

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan is not None
    assert plan.technique == CommunicationTechnique.REFLECTING


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

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan is None


def test_plan_conversation_strategy_unknown(monkeypatch):
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
                    return {
                        "choices": [
                            {
                                "message": {
                                    "content": '{"reasoning":"?","technique":"nonsense"}'
                                }
                            }
                        ]
                    }

            return Resp()

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(
        plan_conversation_strategy("ctx", "hi", previous_ai_text="Hello")
    )
    assert plan.technique == CommunicationTechnique.PROBING

    plan_question = asyncio.run(
        plan_conversation_strategy("ctx", "hi", previous_ai_text="Okay?")
    )
    assert plan_question.technique == CommunicationTechnique.NEUTRAL_ACKNOWLEDGEMENT


def test_plan_conversation_strategy_fuzzy_match(monkeypatch):
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
                    return {
                        "choices": [
                            {
                                "message": {
                                    "content": '{"reasoning":"ok","technique":"sumarizing"}'
                                }
                            }
                        ]
                    }

            return Resp()

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan.technique == CommunicationTechnique.SUMMARIZING


def test_plan_conversation_strategy_long_context(monkeypatch):
    calls = []
    counter = {"i": 0}

    class DummyClient:
        async def __aenter__(self):
            return self

        async def __aexit__(self, exc_type, exc, tb):
            pass

        async def post(self, url, headers=None, json=None, timeout=None):
            counter["i"] += 1
            calls.append(json["messages"][0]["content"])

            class Resp:
                def __init__(self, content):
                    self._content = content

                def raise_for_status(self):
                    pass

                def json(self):
                    if counter["i"] == 1:
                        return {"choices": [{"message": {"content": "summary"}}]}
                    return {
                        "choices": [
                            {"message": {"content": '{"technique":"clarifying"}'}}
                        ]
                    }

            return Resp("")

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", lambda: DummyClient()
    )

    long_ctx = "x" * 4000
    plan = asyncio.run(plan_conversation_strategy(long_ctx, "hi"))
    assert counter["i"] == 2
    assert "Ringkas konteks" in calls[0]
    assert plan.technique == CommunicationTechnique.CLARIFYING


def test_plan_conversation_strategy_perfect_json(monkeypatch):
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
                    return {
                        "choices": [
                            {
                                "message": {
                                    "content": '{"reasoning":"ok","technique":"Reflecting"}'
                                }
                            }
                        ]
                    }

            return Resp()

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan is not None
    assert plan.technique == CommunicationTechnique.REFLECTING


def test_plan_conversation_strategy_synonym(monkeypatch):
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
                    return {
                        "choices": [
                            {
                                "message": {
                                    "content": '{"reasoning":"ok","technique":"reflection"}'
                                }
                            }
                        ]
                    }

            return Resp()

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan.technique == CommunicationTechnique.REFLECTING


def test_plan_conversation_strategy_case_insensitive(monkeypatch):
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
                    return {
                        "choices": [
                            {
                                "message": {
                                    "content": '{"technique":"SuMmAriZinG"}'
                                }
                            }
                        ]
                    }

            return Resp()

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan.technique == CommunicationTechnique.SUMMARIZING


def test_plan_conversation_strategy_unknown_value(monkeypatch):
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
                    return {
                        "choices": [
                            {
                                "message": {
                                    "content": '{"technique":"unknown"}'
                                }
                            }
                        ]
                    }

            return Resp()

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan.technique == CommunicationTechnique.PROBING


def test_plan_conversation_strategy_invalid_json(monkeypatch):
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
                    return {
                        "choices": [
                            {
                                "message": {
                                    "content": '{"technique":"reflecting",}'
                                }
                            }
                        ]
                    }

            return Resp()

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan is None


def test_plan_conversation_strategy_non_json(monkeypatch):
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
                    return {
                        "choices": [
                            {
                                "message": {
                                    "content": "I am not JSON"
                                }
                            }
                        ]
                    }

            return Resp()

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan is None


def test_plan_conversation_strategy_missing_key(monkeypatch):
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
                    return {
                        "choices": [
                            {
                                "message": {
                                    "content": '{"reasoning":"ok"}'
                                }
                            }
                        ]
                    }

            return Resp()

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan is None


def test_plan_conversation_strategy_connection_failure(monkeypatch):
    class DummyClient:
        async def __aenter__(self):
            return self

        async def __aexit__(self, exc_type, exc, tb):
            pass

        async def post(self, url, headers=None, json=None, timeout=None):
            raise httpx.RequestError("connection failed", request=None)

    monkeypatch.setattr(
        "app.services.conversation_planner.httpx.AsyncClient", DummyClient
    )
    plan = asyncio.run(plan_conversation_strategy("ctx", "hi"))
    assert plan is None
