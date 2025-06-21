import asyncio
from .celery_app import celery_app
from app.db.session import SessionLocal
from app import crud
from app.services.sentiment_emotion_analyzer import analyze_sentiment_and_emotions

@celery_app.task
def process_journal_sentiment(journal_id: int):
    db = SessionLocal()
    try:
        journal = crud.journal.get(db=db, id=journal_id)
        if journal:
            result = asyncio.run(analyze_sentiment_and_emotions(journal.content))
            if result:
                journal.sentiment_score = result.get("sentiment_score")
                journal.key_emotions = result.get("emotions")
                journal.primary_emotion = result.get("primary_emotion")
                db.add(journal)
                db.commit()
                db.refresh(journal)
    finally:
        db.close()

@celery_app.task
def process_chat_sentiment(chat_id: int):
    db = SessionLocal()
    try:
        msg = crud.chat_message.get(db=db, id=chat_id)
        if msg:
            result = asyncio.run(analyze_sentiment_and_emotions(msg.text))
            if result:
                msg.sentiment_score = result.get("sentiment_score")
                msg.key_emotions = result.get("emotions")
                msg.primary_emotion = result.get("primary_emotion")
                db.add(msg)
                db.commit()
                db.refresh(msg)
    finally:
        db.close()
