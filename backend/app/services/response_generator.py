from __future__ import annotations

import httpx
import structlog

from app.core.config import settings
from app.schemas.conversation import ConversationPlan
from app.services.conversation_planner import TOOLBOX


async def generate_pure_response(
    plan: ConversationPlan,
    user_message: str,
    context: str,
    persona_trait: str,
) -> str:
    """Generate a plain text reply applying the given conversation technique."""
    log = structlog.get_logger(__name__)
    technique = plan.technique_to_use
    instruction = TOOLBOX.get(plan.technique, "")
    prompt = f"""
Anda adalah Kai, teman yang hangat, empatik, dan tidak menghakimi. {persona_trait}
Sang direktur percakapan memutuskan Anda harus menggunakan teknik '{plan.technique.value}' saat menjawab. Untuk menerapkannya, {instruction}.
Balaslah langsung kepada pengguna dalam Bahasa Indonesia tanpa format atau JSON apa pun.

Konteks:\n{context}

Pesan pengguna:\n{user_message}
"""

    headers = {
        "Authorization": f"Bearer {settings.AI_API_KEY}",
        "HTTP-Referer": settings.AI_HTTP_REFERER,
        "X-Title": settings.AI_TITLE,
        "Content-Type": "application/json",
    }
    body = {
        "model": settings.AI_GENERATOR_MODEL,
        "messages": [{"role": "user", "content": prompt}],
    }

    log.info(
        "generator_request",
        technique=technique,
        user_message=user_message,
        context_length=len(context),
    )

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
            reply = data["choices"][0]["message"]["content"].strip()
            log.info("generator_success", reply=reply)
            return reply
    except (httpx.RequestError, httpx.HTTPStatusError, KeyError) as e:
        log.error("generator_error", error=str(e))
        return ""
