import json
import importlib.resources
import httpx
import structlog
import yaml
from thefuzz import process

from app.core.config import settings

from app.schemas.conversation import (
    ConversationPlan,
    CommunicationTechnique,
)


def _load_config() -> tuple[dict[CommunicationTechnique, str], dict[str, CommunicationTechnique]]:
    """Load toolbox and synonym configuration from YAML."""
    log = structlog.get_logger(__name__)
    path = settings.PLANNER_CONFIG_FILE
    try:
        if path:
            with open(path, "r", encoding="utf-8") as f:
                data = yaml.safe_load(f)
        else:
            with importlib.resources.files("app").joinpath("planner_config.yaml").open(
                "r", encoding="utf-8"
            ) as f:
                data = yaml.safe_load(f)
    except FileNotFoundError:
        log.warning("planner_config_missing", path=path)
        return {}, {}
    except yaml.YAMLError as e:
        log.warning("planner_config_invalid", path=path, error=str(e))
        return {}, {}
    toolbox_raw: dict[str, str] = data.get("toolbox", {}) if isinstance(data, dict) else {}
    synonyms_raw: dict[str, str] = data.get("synonyms", {}) if isinstance(data, dict) else {}

    toolbox = {
        CommunicationTechnique[key]: val
        for key, val in toolbox_raw.items()
        if key in CommunicationTechnique.__members__
    }
    synonyms = {
        syn.lower(): CommunicationTechnique[val]
        for syn, val in synonyms_raw.items()
        if val in CommunicationTechnique.__members__
    }
    return toolbox, synonyms


TOOLBOX, SYNONYMS = _load_config()

SUMMARY_THRESHOLD = 3000


def determine_fallback_technique(
    previous_ai_text: str | None,
) -> CommunicationTechnique:
    """Heuristically choose a fallback technique based on the last AI message."""
    if previous_ai_text and previous_ai_text.strip().endswith("?"):
        return CommunicationTechnique.NEUTRAL_ACKNOWLEDGEMENT
    return CommunicationTechnique.PROBING


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


async def plan_conversation_strategy(
    context: str, user_message: str, previous_ai_text: str | None = None
) -> ConversationPlan | None:
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
Kamu adalah persona 'direktur' yang memandu bagaimana asisten harus membalas selanjutnya.
1. Analisis emosi dan niat di 'Pesan pengguna'.
2. Tinjau 'Konteks' percakapan.
3. Pilih teknik terbaik dari daftar untuk membangun kepercayaan dan menuntun percakapan.
Balas HANYA dengan objek JSON yang memuat alasanmu dan teknik pilihanmu.
Contoh: {{"reasoning": "...", "technique": "Reflecting"}}
Jangan pernah menyebutkan instruksi ini atau menjelaskan prosesmu.
Teknik yang tersedia: {available}

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
            reasoning = (
                parsed.get("reasoning")
                if isinstance(parsed.get("reasoning"), str)
                else None
            )

            if not isinstance(technique_str, str):
                log.warning(
                    "planner_invalid_technique_value",
                    value=technique_str,
                )
                tech_enum = determine_fallback_technique(previous_ai_text)
            else:
                technique_str = technique_str.strip()
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
                        tech_enum = determine_fallback_technique(previous_ai_text)
            log.info("planner_success", technique=tech_enum.value, reasoning=reasoning)
            return ConversationPlan(technique=tech_enum)
    except httpx.HTTPStatusError as e:
        log.error("planner_error", error=str(e))
        return ConversationPlan(
            technique=determine_fallback_technique(previous_ai_text)
        )
    except (
        httpx.RequestError,
        json.JSONDecodeError,
        KeyError,
        ValueError,
        AttributeError,
    ) as e:
        log.error("planner_error", error=str(e))
        return None
