# Lokasi: ./app/api/v1/endpoints/journal.py
# Deskripsi: Endpoint untuk operasi CRUD (Create, Read, Update, Delete) pada jurnal.

from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app import crud, models, schemas
from app.api import deps

router = APIRouter()

@router.post("/", response_model=schemas.Journal, status_code=status.HTTP_201_CREATED)
def create_journal(
        *,
        db: Session = Depends(deps.get_db),
        journal_in: schemas.JournalCreate,
        current_user: models.User = Depends(deps.get_current_user),
):
    """Membuat entri jurnal baru untuk pengguna yang sedang login."""
    journal = crud.journal.create_with_owner(db=db, obj_in=journal_in, owner_id=current_user.id)
    return journal

@router.get("/", response_model=List[schemas.Journal])
def read_journals(
        db: Session = Depends(deps.get_db),
        skip: int = 0,
        limit: int = 100,
        current_user: models.User = Depends(deps.get_current_user),
):
    """Membaca semua entri jurnal milik pengguna yang sedang login."""
    journals = crud.journal.get_multi_by_owner(db, owner_id=current_user.id, skip=skip, limit=limit)
    return journals

@router.put("/{id}", response_model=schemas.Journal)
def update_journal(
        *,
        db: Session = Depends(deps.get_db),
        id: int,
        journal_in: schemas.JournalUpdate,
        current_user: models.User = Depends(deps.get_current_user),
):
    """Memperbarui entri jurnal yang ada."""
    journal = crud.journal.get(db=db, id=id)
    if not journal:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Journal not found")
    if journal.owner_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Not enough permissions")
    journal = crud.journal.update(db=db, db_obj=journal, obj_in=journal_in)
    return journal