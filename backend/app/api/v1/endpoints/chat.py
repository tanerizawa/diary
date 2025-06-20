from typing import List
from fastapi import APIRouter, Depends, HTTPException, status, Body, WebSocket, WebSocketDisconnect
from sqlalchemy.orm import Session
import asyncio
import json

from app import crud, models, schemas
from app.schemas.chat_message import ChatMessageDeleteRequest
from app.api import deps
from app.services.chat_responder import get_ai_reply
from app.services.sentiment_analyzer import analyze_sentiment_with_ai
from app.services.emotion_classifier import detect_mood
from app.services import build_chat_context, analyze_message
from app.tasks import process_chat_sentiment

router = APIRouter()

@router.post(
    "/",
    response_model=schemas.ChatResponse,
    summary="Chat with AI",
    description="Send a message and get a short AI-generated reply with sentiment analysis.",
)
async def chat_with_ai(
    *,
    db: Session = Depends(deps.get_db),
    chat_in: schemas.ChatRequest,
    current_user: models.User = Depends(deps.get_current_user),
):
    """Return a short AI reply based on user's message and context.
    Adds a short delay to mimic typing behavior. This delay can be
    removed or shortened once the client implements its own waiting
    logic so responses remain snappy."""
    analysis = await analyze_message(chat_in.message)
    context = build_chat_context(db, current_user, chat_in.message)

    reply = await get_ai_reply(
        chat_in.message,
        context=context,
        relationship_level=current_user.relationship_level,
        analysis=analysis,
    )
    if reply is None:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="AI service error")
    action = reply["action"]
    reply_text = reply["text_response"]
    journal_template = reply.get("journal_template")
    # Persist conversation
    user_msg = schemas.ChatMessageCreate(
        text=chat_in.message,
        is_user=True,
        timestamp=int(asyncio.get_event_loop().time() * 1000),
    )
    created_user_msg = crud.chat_message.create_with_owner(
        db, obj_in=user_msg, owner_id=current_user.id
    )

    # Sentiment analysis is now handled asynchronously via Celery
    process_chat_sentiment.delay(created_user_msg.id)

    ai_msg = schemas.ChatMessageCreate(
        text=reply_text,
        is_user=False,
        timestamp=int(asyncio.get_event_loop().time() * 1000),
    )
    created_ai_msg = crud.chat_message.create_with_owner(
        db, obj_in=ai_msg, owner_id=current_user.id
    )

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
        ),
        owner_id=current_user.id,
    )

    # Removed artificial delay to improve responsiveness.
    return schemas.ChatResponse(
        message_id=created_user_msg.id,
        ai_message_id=created_ai_msg.id,
        action=action,
        text_response=reply_text,
        sentiment_score=None,
        key_emotions=None,
        detected_mood=detected_mood,
        issue_type=analysis.get("issue_type") if analysis else None,
        recommended_technique=analysis.get("technique") if analysis else None,
        tone=analysis.get("tone") if analysis else None,
        journal_template=journal_template,
    )


@router.post(
    "/messages",
    response_model=schemas.ChatResponse,
    summary="Create message",
    description="Store a chat message and receive an AI response plus sentiment data.",
)
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

    reply = await get_ai_reply(
        message_in.text,
        relationship_level=current_user.relationship_level,
    )
    if reply is None:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="AI service error")
    action = reply["action"]
    reply_text = reply["text_response"]
    journal_template = reply.get("journal_template")

    # The simple message endpoint does not store AI responses

    return schemas.ChatResponse(
        message_id=created_msg.id,
        ai_message_id=None,
        action=action,
        text_response=reply_text,
        sentiment_score=analysis_result.get("sentiment_score") if analysis_result else None,
        key_emotions=analysis_result.get("key_emotions") if analysis_result else None,
        detected_mood=detected_mood,
        issue_type=None,
        recommended_technique=None,
        tone=None,
        journal_template=journal_template,
    )


@router.post(
    "/prompt",
    response_model=schemas.ChatResponse,
    summary="Generate prompt",
    description="Let the AI start the conversation when you're inactive.",
)
async def prompt_chat(
    *,
    db: Session = Depends(deps.get_db),
    current_user: models.User = Depends(deps.get_current_user),
):
    """Generate a proactive AI message using recent context."""
    # Rate limit: avoid sending if last auto prompt was within 6 hours
    recent = crud.chat_message.get_recent_messages(db, owner_id=current_user.id, limit=10)
    last_prompt_ts = None
    for i, msg in enumerate(recent):
        if not msg.is_user:
            prev = recent[i + 1] if i + 1 < len(recent) else None
            if prev is None or not prev.is_user:
                last_prompt_ts = msg.timestamp
                break

    now_ts = int(asyncio.get_event_loop().time() * 1000)
    if last_prompt_ts and now_ts - last_prompt_ts < 6 * 60 * 60 * 1000:
        raise HTTPException(status_code=status.HTTP_429_TOO_MANY_REQUESTS, detail="Prompt recently generated")

    context = build_chat_context(db, current_user)
    context += "\nAkhiri jawaban dengan pertanyaan singkat yang bersifat probing."

    reply = await get_ai_reply(
        "", context=context, relationship_level=current_user.relationship_level
    )
    if reply is None:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="AI service error")
    action = reply["action"]
    reply_text = reply["text_response"]
    journal_template = reply.get("journal_template")

    ai_msg = schemas.ChatMessageCreate(
        text=reply_text,
        is_user=False,
        timestamp=now_ts,
    )
    created_ai_msg = crud.chat_message.create_with_owner(
        db, obj_in=ai_msg, owner_id=current_user.id
    )

    return schemas.ChatResponse(
        message_id=created_ai_msg.id,
        ai_message_id=created_ai_msg.id,
        action=action,
        text_response=reply_text,
        journal_template=journal_template,
    )


@router.get(
    "/messages",
    response_model=List[schemas.ChatMessage],
    summary="Read messages",
    description="Fetch chat history for the current user.",
)
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


@router.delete(
    "/messages",
    response_model=int,
    summary="Delete messages",
    description="Remove one or more chat messages by ID.",
)
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


@router.websocket("/ws")
async def websocket_chat(websocket: WebSocket, token: str):
    """Realtime chat endpoint using WebSocket."""
    await websocket.accept()
    db = deps.SessionLocal()
    try:
        user = deps.authenticate_token(db, token)
        if user is None:
            await websocket.close(code=status.WS_1008_POLICY_VIOLATION)
            return
        while True:
            try:
                text = await websocket.receive_text()
            except WebSocketDisconnect:
                break

            analysis = await analyze_message(text)
            context = build_chat_context(db, user, text)
            reply = await get_ai_reply(
                text,
                context=context,
                relationship_level=user.relationship_level,
                analysis=analysis,
            )
            if reply is None:
                await websocket.send_json({"error": "AI service error"})
                continue

            action = reply["action"]
            reply_text = reply["text_response"]
            journal_template = reply.get("journal_template")

            user_msg = schemas.ChatMessageCreate(
                text=text,
                is_user=True,
                timestamp=int(asyncio.get_event_loop().time() * 1000),
            )
            created_user_msg = crud.chat_message.create_with_owner(
                db, obj_in=user_msg, owner_id=user.id
            )

            process_chat_sentiment.delay(created_user_msg.id)

            ai_msg = schemas.ChatMessageCreate(
                text=reply_text,
                is_user=False,
                timestamp=int(asyncio.get_event_loop().time() * 1000),
            )
            created_ai_msg = crud.chat_message.create_with_owner(
                db, obj_in=ai_msg, owner_id=user.id
            )

            detected_mood = detect_mood(text)
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
                    source_text=text,
                    source_feature="chat_home",
                ),
                owner_id=user.id,
            )

            resp = schemas.ChatResponse(
                message_id=created_user_msg.id,
                ai_message_id=created_ai_msg.id,
                action=action,
                text_response=reply_text,
                sentiment_score=None,
                key_emotions=None,
                detected_mood=detected_mood,
                issue_type=analysis.get("issue_type") if analysis else None,
                recommended_technique=analysis.get("technique") if analysis else None,
                tone=analysis.get("tone") if analysis else None,
                journal_template=journal_template,
            )
            await websocket.send_text(resp.model_dump_json())
    finally:
        db.close()
