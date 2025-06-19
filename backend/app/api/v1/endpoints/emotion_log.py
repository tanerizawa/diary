from typing import List
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from app import crud, models, schemas
from app.api import deps

router = APIRouter()

@router.post("/", response_model=schemas.EmotionLog)
def create_emotion_log(
    *,
    db: Session = Depends(deps.get_db),
    log_in: schemas.EmotionLogCreate,
    current_user: models.User = Depends(deps.get_current_user),
):
    return crud.emotion_log.create_with_owner(db, obj_in=log_in, owner_id=current_user.id)


@router.get("/", response_model=List[schemas.EmotionLog])
def read_emotion_logs(
    *,
    db: Session = Depends(deps.get_db),
    skip: int = 0,
    limit: int = 100,
    current_user: models.User = Depends(deps.get_current_user),
):
    return crud.emotion_log.get_multi_by_owner(db, owner_id=current_user.id, skip=skip, limit=limit)
