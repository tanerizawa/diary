from typing import Generator
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import jwt, JWTError
from pydantic import ValidationError
from sqlalchemy.orm import Session

from app import crud, models, schemas
# PERBAIKAN: Impor 'settings' dari config
from app.core.config import settings
from app.db.session import SessionLocal

reusable_oauth2 = OAuth2PasswordBearer(
    tokenUrl=f"{settings.API_V1_STR}/users/login"
)

def get_db() -> Generator:
    """Dependensi untuk mendapatkan sesi database."""
    try:
        db = SessionLocal()
        yield db
    finally:
        db.close()

def get_current_user(
        db: Session = Depends(get_db), token: str = Depends(reusable_oauth2)
) -> models.User:
    """Dependensi untuk mendapatkan pengguna saat ini dari token JWT."""
    try:
        payload = jwt.decode(
            token,
            settings.SECRET_KEY,
            # PERBAIKAN: Mengambil ALGORITHM dari settings, bukan security
            algorithms=[settings.ALGORITHM]
        )
        token_data = schemas.TokenData.model_validate(payload)
    except (JWTError, ValidationError) as e:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=f"Could not validate credentials: {e}",
        )

    # PERBAIKAN: Menggunakan token_data.sub yang merupakan email
    if token_data.sub is None:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Could not validate credentials, subject missing.",
        )

    user = crud.user.get_by_email(db, email=token_data.sub)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    if not user.is_active:
        raise HTTPException(status_code=400, detail="Inactive user")
    return user
