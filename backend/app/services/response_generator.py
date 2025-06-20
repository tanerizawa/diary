from __future__ import annotations

import httpx

from app.core.config import settings
from app.schemas.conversation import ConversationPlan


async def generate_pure_response(plan: ConversationPlan, user_message: str) -> str:
    """Generate a plain text reply applying the given conversation technique."""
    technique = plan.technique_to_use
    prompt = f"""
You are the 'actor' persona executing the assistant's reply.
Follow the director's instructions exactly and use this technique: {technique}.
Respond directly to the user in Bahasa Indonesia without any JSON or formatting.

User message:\n{user_message}
"""

    headers = {
        "Authorization": f"Bearer {settings.AI_API_KEY}",
        "Content-Type": "application/json",
    }
    body = {
        "model": settings.AI_GENERATOR_MODEL,
        "messages": [{"role": "user", "content": prompt}],
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
            return data["choices"][0]["message"]["content"].strip()
    except (httpx.RequestError, httpx.HTTPStatusError, KeyError) as e:
        print(f"Error calling response generator: {e}")
        return ""
