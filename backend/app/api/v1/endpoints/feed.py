from typing import List
from datetime import datetime, timedelta

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app import crud, models, schemas
from app.api import deps

router = APIRouter()

@router.get("/", response_model=List[schemas.FeedItem])
def read_feed(
    *,
    db: Session = Depends(deps.get_db),
    current_user: models.User = Depends(deps.get_current_user),
):
    """Generate a simple personalized feed."""
    journals = crud.journal.get_multi_by_owner(db, owner_id=current_user.id, limit=5)
    items: List[schemas.FeedItem] = [
        schemas.FeedItem(type="journal", journal=j) for j in journals
    ]

    last_journal = journals[0] if journals else None
    if last_journal and (last_journal.sentiment_score or 0) < 0:
        items.append(
            schemas.FeedItem(
                type="article_suggestion",
                article=schemas.Article(
                    title="Mengelola Pikiran Negatif",
                    source="Psikologi+",
                    image_url="https://placehold.co/600x400/D3E4F7/001D35?text=Artikel",
                ),
            )
        )

    last_msg = crud.chat_message.get_last_user_messages(db, owner_id=current_user.id, limit=1)
    now_ms = int(datetime.utcnow().timestamp() * 1000)
    if not last_msg or now_ms - last_msg[0].timestamp > 86_400_000:
        items.append(
            schemas.FeedItem(
                type="chat_prompt",
                message="Coba ngobrol dengan Dear Diary Chat hari ini!",
            )
        )

    hour = datetime.now().hour
    if hour >= 22 or hour < 6:
        items.append(
            schemas.FeedItem(
                type="article_suggestion",
                article=schemas.Article(
                    title="Meditasi sebelum Tidur",
                    source="HelloSehat",
                    image_url="https://placehold.co/600x400/D3E4F7/001D35?text=Artikel",
                ),
            )
        )

    return items
