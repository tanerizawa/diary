import json
import httpx
import structlog

from app.core.config import settings

from app.schemas.conversation import (
    ConversationPlan,
    CommunicationTechnique,
)

TOOLBOX: dict[CommunicationTechnique, str] = {
    CommunicationTechnique.PROBING: "ask short clarifying questions",
    CommunicationTechnique.CLARIFYING: "confirm your understanding",
    CommunicationTechnique.PARAPHRASING: "rephrase the user's message",
    CommunicationTechnique.REFLECTING: "mirror the user's feelings",
    CommunicationTechnique.OPEN_ENDED_QUESTIONS: "invite more details",
    CommunicationTechnique.CLOSED_ENDED_QUESTIONS: "get specific facts",
    CommunicationTechnique.SUMMARIZING: "briefly recap key points",
    CommunicationTechnique.CONFRONTATION: "gently note inconsistencies",
    CommunicationTechnique.REASSURANCE_ENCOURAGEMENT: "offer reassurance",
}

SYNONYMS = {
    "reflection": CommunicationTechnique.REFLECTING,
    "mirroring": CommunicationTechnique.REFLECTING,
}

async def plan_conversation_strategy(context: str, user_message: str) -> ConversationPlan | None:
    """Request a conversation technique suggestion from the AI service."""
    log = structlog.get_logger(__name__)
    log.info(
        "planning_conversation",
        context_length=len(context),
        user_message=user_message,
    )
    available = ", ".join(t.value for t in CommunicationTechnique)
    prompt = f"""
You are the 'director' persona guiding how the assistant should reply next.
Choose one communication technique from the following list and respond only with JSON key 'technique'.
Available techniques: {available}

Context:\n{context}

User message:\n{user_message}
"""

    headers = {
        "Authorization": f"Bearer {settings.AI_API_KEY}",
        "HTTP-Referer": settings.AI_HTTP_REFERER,
        "X-Title": settings.AI_TITLE,
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
            if not isinstance(parsed, dict):
                raise ValueError("Invalid JSON structure")
            technique_str = parsed.get("technique")
            if not isinstance(technique_str, str):
                raise ValueError("Invalid technique")
            lower = technique_str.lower()
            tech_enum = SYNONYMS.get(lower)
            if tech_enum is None:
                tech_enum = next(
                    (
                        t
                        for t in CommunicationTechnique
                        if lower in {t.value.lower(), t.name.lower()}
                    ),
                    CommunicationTechnique.PROBING,
                )
            log.info("planner_success", technique=tech_enum.value)
            return ConversationPlan(technique=tech_enum)
    except (httpx.RequestError, httpx.HTTPStatusError, json.JSONDecodeError, KeyError, ValueError, AttributeError) as e:
        log.error("planner_error", error=str(e))
        return None
