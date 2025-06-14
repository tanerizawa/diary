# File: app/crud/crud_user.py
# Deskripsi: Berisi fungsi-fungsi CRUD (Create, Read, Update, Delete) untuk model User.

from sqlalchemy.orm import Session
from app import models, schemas
# Impor dari lokasi baru yang benar
from app.core.hashing import get_password_hash

def get_user_by_email(db: Session, email: str) -> models.User | None:
    """
    Mengambil satu pengguna dari database berdasarkan alamat email.
    """
    return db.query(models.User).filter(models.User.email == email).first()

def create_user(db: Session, user: schemas.UserCreate) -> models.User:
    """
    Membuat pengguna baru di database.
    """
    hashed_password = get_password_hash(user.password)
    db_user = models.User(email=user.email, hashed_password=hashed_password)
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user