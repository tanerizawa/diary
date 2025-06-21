import httpx
import json
from app.core.config import settings

async def analyze_sentiment_and_emotions(text: str) -> dict | None:
    """Analyze *text* for sentiment and emotions using the AI service."""
    prompt = f"""
    Analyze the sentiment and emotions of the following text.
    Respond only with raw JSON having these keys:
    sentiment_score - float between -1.0 and 1.0
    emotions - comma separated list of emotions present
    primary_emotion - the single emotion that best represents the text

    Text: "{text}"
    """
    headers = {
        "Authorization": f"Bearer {settings.AI_API_KEY}",
        "HTTP-Referer": settings.AI_HTTP_REFERER,
        "X-Title": settings.AI_TITLE,
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
                "sentiment_score": result.get("sentiment_score"),
                "emotions": result.get("emotions"),
                "primary_emotion": result.get("primary_emotion"),
            }
    except (httpx.RequestError, httpx.HTTPStatusError, json.JSONDecodeError, KeyError) as e:
        print(f"Error calling sentiment/emotion service: {e}")
        return None
