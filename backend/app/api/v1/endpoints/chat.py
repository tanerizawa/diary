from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
import asyncio

from app import crud, models, schemas
from app.api import deps
from app.services.chat_responder import get_ai_reply

router = APIRouter()

@router.post("/", response_model=schemas.ChatResponse)
async def chat_with_ai(
    *,
    db: Session = Depends(deps.get_db),
    chat_in: schemas.ChatRequest,
    current_user: models.User = Depends(deps.get_current_user),
):
    """Return a short AI reply based on user's message and context.
    Adds a small delay to mimic typing behavior."""
    journals = crud.journal.get_multi_by_owner(db, owner_id=current_user.id, limit=5)
    context_lines = [j.content for j in journals]
    moods: dict[str, int] = {}
    for j in journals:
        moods[j.mood] = moods.get(j.mood, 0) + 1
    mood_summary = ", ".join(f"{m}:{c}" for m, c in moods.items())
    context = "Recent journal entries:\n" + "\n".join(context_lines)
    if mood_summary:
        context += f"\nMood frequencies: {mood_summary}"

    reply = await get_ai_reply(chat_in.message, context=context)
    if reply is None:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="AI service error")
    await asyncio.sleep(2)
    return schemas.ChatResponse(reply=reply)
