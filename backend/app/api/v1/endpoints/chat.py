from typing import List
from fastapi import APIRouter, Depends, HTTPException, status, BackgroundTasks, Body
from sqlalchemy.orm import Session
import asyncio

from app import crud, models, schemas
from app.schemas.chat_message import ChatMessageDeleteRequest
from app.api import deps
from app.services.chat_responder import get_ai_reply
from app.services.sentiment_analyzer import analyze_sentiment_with_ai
from app.services.emotion_classifier import detect_mood

router = APIRouter()

@router.post("/", response_model=schemas.ChatResponse)
async def chat_with_ai(
    *,
    db: Session = Depends(deps.get_db),
    chat_in: schemas.ChatRequest,
    background_tasks: BackgroundTasks,
    current_user: models.User = Depends(deps.get_current_user),
):
    """Return a short AI reply based on user's message and context.
    Adds a short delay to mimic typing behavior. This delay can be
    removed or shortened once the client implements its own waiting
    logic so responses remain snappy."""
    journals = crud.journal.get_multi_by_owner(db, owner_id=current_user.id, limit=5)
    context_lines = [j.content for j in journals]
    moods: dict[str, int] = {}
    for j in journals:
        moods[j.mood] = moods.get(j.mood, 0) + 1

    recent_msgs = crud.chat_message.get_last_user_messages(db, owner_id=current_user.id, limit=4)
    message_lines = [m.text for m in recent_msgs]

    mood_summary = ", ".join(f"{m}:{c}" for m, c in moods.items())
    context_sections = []
    if context_lines:
        context_sections.append("Recent journal entries:\n" + "\n".join(context_lines))
    if message_lines:
        context_sections.append("Recent conversation:\n" + "\n".join(message_lines))
    if mood_summary:
        context_sections.append(f"Mood frequencies: {mood_summary}")
    context = "\n".join(context_sections)

    reply = await get_ai_reply(chat_in.message, context=context)
    if reply is None:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="AI service error")
    # Persist conversation
    user_msg = schemas.ChatMessageCreate(
        text=chat_in.message,
        is_user=True,
        timestamp=int(asyncio.get_event_loop().time() * 1000),
    )
    created_user_msg = crud.chat_message.create_with_owner(
        db, obj_in=user_msg, owner_id=current_user.id
    )

    analysis_result = await analyze_sentiment_with_ai(chat_in.message)
    if analysis_result:
        crud.chat_message.update(
            db,
            db_obj=created_user_msg,
            obj_in={
                "sentiment_score": analysis_result.get("sentiment_score"),
                "key_emotions": analysis_result.get("key_emotions"),
            },
        )
    background_tasks.add_task(
        crud.chat_message.process_and_update_sentiment, chat_id=created_user_msg.id
    )

    ai_msg = schemas.ChatMessageCreate(
        text=reply,
        is_user=False,
        timestamp=int(asyncio.get_event_loop().time() * 1000),
    )
    crud.chat_message.create_with_owner(db, obj_in=ai_msg, owner_id=current_user.id)

    # Classify user's mood using the heuristic classifier
    detected_mood = detect_mood(chat_in.message)
    crud.chat_message.update(
        db,
        db_obj=created_user_msg,
        obj_in={"detected_mood": detected_mood},
    )
    crud.emotion_log.create_with_owner(
        db,
        obj_in=schemas.EmotionLogCreate(
            timestamp=int(asyncio.get_event_loop().time() * 1000),
            detected_mood=detected_mood,
            source_text=chat_in.message,
            source_feature="chat_home",
            sentiment_score=analysis_result.get("sentiment_score") if analysis_result else None,
            key_emotions_detected=(analysis_result.get("key_emotions").split(",") if analysis_result and analysis_result.get("key_emotions") else None),
        ),
        owner_id=current_user.id,
    )

    # Removed artificial delay to improve responsiveness.
    return schemas.ChatResponse(
        reply_text=reply,
        sentiment_score=analysis_result.get("sentiment_score") if analysis_result else None,
        key_emotions=analysis_result.get("key_emotions") if analysis_result else None,
        detected_mood=detected_mood,
    )


@router.post("/messages", response_model=schemas.ChatResponse)
async def create_message(
    *,
    db: Session = Depends(deps.get_db),
    message_in: schemas.ChatMessageCreate,
    current_user: models.User = Depends(deps.get_current_user),
):
    """Create a chat message and return an AI reply with sentiment data."""

    created_msg = crud.chat_message.create_with_owner(
        db=db, obj_in=message_in, owner_id=current_user.id
    )

    analysis_result = await analyze_sentiment_with_ai(message_in.text)
    detected_mood = detect_mood(message_in.text)

    crud.chat_message.update(
        db,
        db_obj=created_msg,
        obj_in={
            "sentiment_score": analysis_result.get("sentiment_score") if analysis_result else None,
            "key_emotions": analysis_result.get("key_emotions") if analysis_result else None,
            "detected_mood": detected_mood,
        },
    )

    crud.emotion_log.create_with_owner(
        db,
        obj_in=schemas.EmotionLogCreate(
            timestamp=message_in.timestamp,
            detected_mood=detected_mood,
            source_text=message_in.text,
            source_feature="chat_home",
            sentiment_score=analysis_result.get("sentiment_score") if analysis_result else None,
            key_emotions_detected=(analysis_result.get("key_emotions").split(",") if analysis_result and analysis_result.get("key_emotions") else None),
        ),
        owner_id=current_user.id,
    )

    reply = await get_ai_reply(message_in.text)
    if reply is None:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="AI service error")

    crud.chat_message.create_with_owner(
        db,
        obj_in=schemas.ChatMessageCreate(
            text=reply,
            is_user=False,
            timestamp=int(asyncio.get_event_loop().time() * 1000),
        ),
        owner_id=current_user.id,
    )

    return schemas.ChatResponse(
        reply_text=reply,
        sentiment_score=analysis_result.get("sentiment_score") if analysis_result else None,
        key_emotions=analysis_result.get("key_emotions") if analysis_result else None,
        detected_mood=detected_mood,
    )


@router.get("/messages", response_model=List[schemas.ChatMessage])
def read_messages(
    *,
    db: Session = Depends(deps.get_db),
    skip: int = 0,
    limit: int = 100,
    current_user: models.User = Depends(deps.get_current_user),
):
    """Fetch chat history for the current user."""
    return crud.chat_message.get_multi_by_owner(
        db, owner_id=current_user.id, skip=skip, limit=limit
    )


@router.delete("/messages", response_model=int)
def delete_messages(
    *,
    db: Session = Depends(deps.get_db),
    delete_in: ChatMessageDeleteRequest = Body(...),
    current_user: models.User = Depends(deps.get_current_user),
):
    """Delete one or more chat messages for the current user.

    Returns the number of messages removed."""
    deleted = crud.chat_message.remove_multi(
        db, ids=delete_in.ids, owner_id=current_user.id
    )
    if deleted == 0:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Messages not found")
    return deleted
