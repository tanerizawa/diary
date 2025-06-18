# LOKASI: app/api/v1/endpoints/journal.py

from typing import List, Any
from fastapi import APIRouter, Depends, HTTPException, status, BackgroundTasks
from sqlalchemy.orm import Session
from app import crud, models, schemas
from app.api import deps

router = APIRouter()


@router.post("/", response_model=schemas.Journal, status_code=status.HTTP_201_CREATED)
def create_journal(
    *,
    db: Session = Depends(deps.get_db),
    background_tasks: BackgroundTasks,
    journal_in: schemas.JournalCreate,
    current_user: models.User = Depends(deps.get_current_user),
):
    """Membuat entri jurnal baru dan memicu analisis sentimen di latar belakang."""
    journal = crud.journal.create_with_owner(
        db=db, obj_in=journal_in, owner_id=current_user.id
    )

    background_tasks.add_task(
        crud.journal.process_and_update_sentiment, journal_id=journal.id
    )

    return journal


@router.get("/", response_model=List[schemas.Journal])
def read_journals(
    # --- PERBAIKAN: Memastikan semua parameter ada di sini ---
    db: Session = Depends(deps.get_db),
    skip: int = 0,
    limit: int = 100,
    current_user: models.User = Depends(deps.get_current_user),
    # --- AKHIR PERBAIKAN ---
):
    """Membaca semua entri jurnal milik pengguna yang sedang login."""
    journals = crud.journal.get_multi_by_owner(
        db, owner_id=current_user.id, skip=skip, limit=limit
    )
    return journals


@router.put("/{id}", response_model=schemas.Journal)
def update_journal(
    *,
    db: Session = Depends(deps.get_db),
    background_tasks: BackgroundTasks,
    id: int,
    journal_in: schemas.JournalUpdate,
    current_user: models.User = Depends(deps.get_current_user),
):
    """Memperbarui entri jurnal dan memicu analisis sentimen ulang di latar belakang."""
    journal = crud.journal.get(db=db, id=id)
    if not journal:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Journal not found"
        )
    if journal.owner_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN, detail="Not enough permissions"
        )

    updated_journal = crud.journal.update(db=db, db_obj=journal, obj_in=journal_in)

    background_tasks.add_task(
        crud.journal.process_and_update_sentiment, journal_id=updated_journal.id
    )

    return updated_journal


@router.delete("/{id}", response_model=schemas.Journal)
def delete_journal(
    *,
    db: Session = Depends(deps.get_db),
    id: int,
    current_user: models.User = Depends(deps.get_current_user),
) -> Any:
    """Menghapus sebuah entri jurnal."""
    journal = crud.journal.get(db=db, id=id)
    if not journal:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND, detail="Journal not found"
        )
    if journal.owner_id != current_user.id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN, detail="Not enough permissions"
        )

    deleted_journal = crud.journal.remove(db=db, id=id)
    return deleted_journal
