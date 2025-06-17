import httpx
from app.core.config import settings

async def get_ai_reply(message: str, context: str = "") -> str | None:
    """Send a chat message to the OpenRouter API and return the reply."""
    system_prompt = (
        context if context else "You are a helpful mental health assistant."
    )
    body = {
        "model": "google/gemini-flash-1.5",
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": message},
        ],
    }
    headers = {
        "Authorization": f"Bearer {settings.AI_API_KEY}",
        "Content-Type": "application/json",
    }
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                url=settings.AI_API_URL,
                headers=headers,
                json=body,
                timeout=30.0,
            )
            response.raise_for_status()
            data = response.json()
            return data["choices"][0]["message"]["content"].strip()
    except (httpx.RequestError, httpx.HTTPStatusError, KeyError) as e:
        print(f"Error calling AI service: {e}")
        return None
