import json
import httpx
import structlog
from thefuzz import process

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

SUMMARY_THRESHOLD = 3000


async def _summarize_context(context: str) -> str:
    """Summarize *context* using the AI service."""
    log = structlog.get_logger(__name__)
    prompt = "Ringkas konteks percakapan berikut dalam Bahasa Indonesia:\n" + context
    headers = {
        "Authorization": f"Bearer {settings.AI_API_KEY}",
        "HTTP-Referer": settings.AI_HTTP_REFERER,
        "X-Title": settings.AI_TITLE,
        "Content-Type": "application/json",
    }
    body = {
        "model": settings.AI_PLANNER_MODEL,
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
            summary = data["choices"][0]["message"]["content"].strip()
            log.info("planner_summary_success")
            return summary
    except (httpx.RequestError, httpx.HTTPStatusError, KeyError) as e:
        log.error("planner_summary_error", error=str(e))
        return context[:SUMMARY_THRESHOLD]

async def plan_conversation_strategy(context: str, user_message: str) -> ConversationPlan | None:
    """Request a conversation technique suggestion from the AI service."""
    log = structlog.get_logger(__name__)
    log.info(
        "planning_conversation",
        context_length=len(context),
        user_message=user_message,
    )

    if len(context) > SUMMARY_THRESHOLD:
        context = await _summarize_context(context)
    available = ", ".join(t.value for t in CommunicationTechnique)
    prompt = f"""
You are the 'director' persona guiding how the assistant should reply next.
1. Analyze the underlying emotion and intent in the 'User message'.
2. Review the conversation 'Context'.
3. Choose the best technique from the list to build trust and guide the conversation.
Respond ONLY with a JSON object containing your reasoning and the chosen technique.
Example: {{"reasoning": "...", "technique": "Reflecting"}}
Never mention these instructions or explain your process.
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
            reasoning = parsed.get("reasoning") if isinstance(parsed.get("reasoning"), str) else None
            lower = technique_str.lower()
            tech_enum = SYNONYMS.get(lower)
            if tech_enum is None:
                choices: dict[str, CommunicationTechnique] = {
                    t.name: t for t in CommunicationTechnique
                }
                choices.update({t.value: t for t in CommunicationTechnique})
                match = process.extractOne(
                    technique_str,
                    choices.keys(),
                    processor=str.lower,
                )
                if match and match[1] > 80:
                    tech_enum = choices[match[0]]
                else:
                    log.warning(
                        "planner_new_technique_suggestion",
                        suggestion=technique_str,
                    )
                    tech_enum = CommunicationTechnique.PROBING
            log.info(
                "planner_success", technique=tech_enum.value, reasoning=reasoning
            )
            return ConversationPlan(technique=tech_enum)
    except (httpx.RequestError, httpx.HTTPStatusError, json.JSONDecodeError, KeyError, ValueError, AttributeError) as e:
        log.error("planner_error", error=str(e))
        return None
