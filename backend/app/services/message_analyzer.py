import httpx
import json
from app.core.config import settings

async def analyze_message(text: str) -> dict | None:
    """Analyze a chat message and return issue type, recommended technique, and tone."""
    prompt = f"""
    Identify the main mental health issue from the following user message.
    Suggest one short coping technique the user could try.
    Guess the user's tone in one word.
    Respond with raw JSON using keys: issue_type, technique, tone.

    Message: "{text}"
    """
    headers = {
        "Authorization": f"Bearer {settings.AI_API_KEY}",
        "Content-Type": "application/json",
    }
    body = {
        "model": settings.AI_MODEL,
        "messages": [{"role": "user", "content": prompt}],
        "response_format": {"type": "json_object"},
    }
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.post(
                url=settings.AI_API_URL,
                headers=headers,
                json=body,
                timeout=30.0,
            )
            resp.raise_for_status()
            data = resp.json()
            content = data["choices"][0]["message"]["content"]
            result = json.loads(content)
            return {
                "issue_type": result.get("issue_type"),
                "technique": result.get("technique"),
                "tone": result.get("tone"),
            }
    except (httpx.RequestError, httpx.HTTPStatusError, json.JSONDecodeError, KeyError) as e:
        print(f"Error calling analysis service: {e}")
        return None
