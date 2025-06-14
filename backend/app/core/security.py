# File: app/core/security.py
# Deskripsi: Berisi fungsi-fungsi terkait keamanan seperti pembuatan/verifikasi token JWT.

# Menghapus 'passlib.context' karena sudah pindah ke hashing.py
from jose import JWTError, jwt
from datetime import datetime, timedelta, timezone
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session

from app import schemas, database
from app.core.config import settings
from app.crud import crud_user
from app.core.hashing import verify_password # <- Impor dari file hashing baru

# Skema otentikasi
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/v1/users/login")

# Fungsi verify_password diimpor dari atas untuk tetap bisa digunakan oleh endpoint login
# Fungsi get_password_hash dihapus karena hanya digunakan oleh crud_user

def create_access_token(data: dict) -> str:
    """Membuat token akses JWT."""
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    return encoded_jwt

async def get_current_user(
        token: str = Depends(oauth2_scheme), db: Session = Depends(database.get_db)
):
    """
    Dependency untuk mendapatkan pengguna yang sedang login berdasarkan token.
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        email: str = payload.get("sub")
        if email is None:
            raise credentials_exception
        token_data = schemas.TokenData(email=email)
    except JWTError:
        raise credentials_exception

    user = crud_user.get_user_by_email(db, email=token_data.email)
    if user is None:
        raise credentials_exception
    return user