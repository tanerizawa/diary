from __future__ import annotations

from datetime import datetime
from sqlalchemy.orm import Session

from app import crud, models
from .embedding import find_similar_entries


def _time_of_day() -> str:
    hour = datetime.now().hour
    if 5 <= hour < 12:
        return "morning"
    if 12 <= hour < 18:
        return "afternoon"
    if 18 <= hour < 22:
        return "evening"
    return "night"


def build_chat_context(db: Session, user: models.User, text: str | None = None) -> str:
    """Assemble a string summarizing user info and recent activity."""
    journals = crud.journal.get_multi_by_owner(db, owner_id=user.id, limit=5)
    context_lines = [j.content for j in journals]

    moods: dict[str, int] = {}
    for j in journals:
        moods[j.mood] = moods.get(j.mood, 0) + 1

    recent_msgs = crud.chat_message.get_last_user_messages(db, owner_id=user.id, limit=4)
    message_lines = [m.text for m in recent_msgs]

    mood_summary = ", ".join(f"{m}:{c}" for m, c in moods.items())

    sections: list[str] = []
    info_parts: list[str] = []
    if user.name:
        info_parts.append(user.name)
    if user.bio:
        info_parts.append(user.bio)
    if user.mbti_type:
        info_parts.append(f"MBTI: {user.mbti_type}")
    if info_parts:
        sections.append("User info: " + ", ".join(info_parts))

    sections.append(f"Time of day: {_time_of_day()}")
    if mood_summary:
        sections.append(f"Mood frequencies: {mood_summary}")
    if text:
        similar = find_similar_entries(db, user, text, limit=3)
        if similar:
            sections.append("Similar entries:\n" + "\n".join(similar))
    else:
        if context_lines:
            sections.append("Recent journal entries:\n" + "\n".join(context_lines))
        if message_lines:
            sections.append("Recent conversation:\n" + "\n".join(message_lines))

    return "\n".join(sections)
