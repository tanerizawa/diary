import json
import asyncio
from app.services.chat_responder import get_ai_reply

def test_get_ai_reply_includes_analysis(monkeypatch):
    captured = {}

    class DummyClient:
        async def __aenter__(self):
            return self
        async def __aexit__(self, exc_type, exc, tb):
            pass
        async def post(self, url, headers=None, json=None, timeout=None):
            captured['messages'] = json['messages']
            class Resp:
                def raise_for_status(self):
                    pass
                def json(self):
                    return {"choices": [{"message": {"content": '{"action":"balas_teks","text_response":"ok"}'}}]}
            return Resp()

    monkeypatch.setattr("app.services.chat_responder.httpx.AsyncClient", DummyClient)
    analysis = {"issue_type": "stress"}
    reply = asyncio.run(get_ai_reply("hi", analysis=analysis))
    assert reply == {"action": "balas_teks", "text_response": "ok"}
    assert captured['messages'][0]['content'] == json.dumps(analysis, ensure_ascii=False)
