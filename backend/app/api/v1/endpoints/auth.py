# Lokasi: ./app/api/v1/endpoints/auth.py
# Deskripsi: Menambahkan endpoint DELETE /me untuk menghapus akun.

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app import crud, models, schemas
from app.api import deps
from app.core.security import create_access_token, verify_password

router = APIRouter()

@router.post("/register", response_model=schemas.User)
def register_user(
        *,
        db: Session = Depends(deps.get_db),
        user_in: schemas.UserCreate,
):
    """Mendaftarkan pengguna baru."""
    user = crud.user.get_by_email(db, email=user_in.email)
    if user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="The user with this email already exists in the system.",
        )
    user = crud.user.create(db, obj_in=user_in)
    return user

@router.post("/login", response_model=schemas.Token)
def login_for_access_token(
        *,
        db: Session = Depends(deps.get_db),
        login_data: schemas.LoginRequest,
):
    """Endpoint untuk login dan mendapatkan access token dari body JSON."""
    user = crud.user.get_by_email(db, email=login_data.email)

    if not user or not verify_password(login_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    elif not user.is_active:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Inactive user")

    access_token = create_access_token(data={"sub": user.email})
    return {"access_token": access_token, "token_type": "bearer"}

# PERBAIKAN: Menambahkan endpoint baru untuk menghapus akun
@router.delete("/me", response_model=schemas.User)
def delete_current_user(
        *,
        db: Session = Depends(deps.get_db),
        current_user: models.User = Depends(deps.get_current_user),
):
    """Menghapus akun pengguna yang sedang login."""
    user = crud.user.remove(db=db, id=current_user.id)
    return user
