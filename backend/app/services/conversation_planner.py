import json
import httpx

from app.core.config import settings

from app.schemas.conversation import ConversationPlan

async def plan_conversation_strategy(context: str, user_message: str) -> ConversationPlan | None:
    """Request a conversation technique suggestion from the AI service."""
    prompt = f"""
You are the 'director' persona guiding how the assistant should reply next.
Given the current conversation context and the latest user message, choose the most suitable communication technique the assistant should use.
Respond only with raw JSON containing a single key named \"technique\".

Context:\n{context}

User message:\n{user_message}
"""

    headers = {
        "Authorization": f"Bearer {settings.AI_API_KEY}",
        "Content-Type": "application/json",
    }
    body = {
        "model": settings.AI_PLANNER_MODEL,
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
            parsed = json.loads(content)
            technique = parsed.get("technique")
            if not isinstance(technique, str):
                raise ValueError("Invalid technique")
            return ConversationPlan(technique=technique)
    except (httpx.RequestError, httpx.HTTPStatusError, json.JSONDecodeError, KeyError, ValueError) as e:
        print(f"Error calling conversation planner: {e}")
        return None
