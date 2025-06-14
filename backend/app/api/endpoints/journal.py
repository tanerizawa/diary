# File: app/api/endpoints/journal.py
# Deskripsi: Router yang menangani semua endpoint terkait jurnal.
# Logika bisnis dipindahkan ke modul CRUD.

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from typing import List

from app import models, schemas, database
from app.core import security
from app.crud import crud_journal # <- Impor crud_journal

router = APIRouter()

@router.post("/journal", response_model=schemas.JournalEntryResponse, status_code=201)
async def create_new_journal_entry(
        journal: schemas.JournalEntryCreate,
        db: Session = Depends(database.get_db),
        current_user: models.User = Depends(security.get_current_user)
):
    """
    Endpoint untuk membuat entri jurnal baru dengan memanggil service layer.
    """
    return await crud_journal.create_journal_entry(
        db=db, entry_in=journal, owner_id=current_user.id
    )

@router.get("/journal", response_model=List[schemas.JournalEntryResponse])
def read_user_journal_entries(
        skip: int = 0,
        limit: int = 100,
        db: Session = Depends(database.get_db),
        current_user: models.User = Depends(security.get_current_user)
):
    """
    Endpoint untuk membaca semua entri jurnal milik pengguna.
    """
    entries = crud_journal.get_user_entries(
        db=db, owner_id=current_user.id, skip=skip, limit=limit
    )
    return entries