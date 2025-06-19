# LOKASI: app/api/v1/endpoints/journal.py

from typing import List, Any
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app import crud, models, schemas
from app.tasks import process_journal_sentiment
from app.api import deps

router = APIRouter()


@router.post(
    "/",
    response_model=schemas.Journal,
    status_code=status.HTTP_201_CREATED,
    summary="Create journal entry",
    description="Write a new journal entry and trigger background sentiment analysis.",
)
def create_journal(
    *,
    db: Session = Depends(deps.get_db),
    journal_in: schemas.JournalCreate,
    current_user: models.User = Depends(deps.get_current_user),
):
    """Membuat entri jurnal baru dan memicu analisis sentimen di latar belakang."""
    journal = crud.journal.create_with_owner(
        db=db, obj_in=journal_in, owner_id=current_user.id
    )

    process_journal_sentiment.delay(journal.id)

    return journal


@router.get(
    "/",
    response_model=List[schemas.Journal],
    summary="Read journals",
    description="List all journal entries belonging to the current user.",
)
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


@router.put(
    "/{id}",
    response_model=schemas.Journal,
    summary="Update journal entry",
    description="Edit an existing journal entry and re-run sentiment analysis.",
)
def update_journal(
    *,
    db: Session = Depends(deps.get_db),
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

    process_journal_sentiment.delay(updated_journal.id)

    return updated_journal


@router.delete(
    "/{id}",
    response_model=schemas.Journal,
    summary="Delete journal entry",
    description="Remove a journal entry permanently.",
)
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
