# Lokasi: ./app/crud/crud_user.py
# Deskripsi: Menambahkan fungsi 'remove' untuk menghapus objek dari database.

from sqlalchemy.orm import Session
from app.crud.base import CRUDBase
from app.db.models.user import User
from app.schemas.user import UserCreate
from app.core.security import get_password_hash

class CRUDUser(CRUDBase[User, UserCreate, UserCreate]):
    def get_by_email(self, db: Session, *, email: str) -> User | None:
        return db.query(User).filter(User.email == email).first()

    def create(self, db: Session, *, obj_in: UserCreate) -> User:
        db_obj = User(
            email=obj_in.email,
            hashed_password=get_password_hash(obj_in.password),
            is_active=True,
            name=obj_in.name,
            bio=obj_in.bio,
            mbti_type=obj_in.mbti_type,
            relationship_level=0,
        )
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    # PERBAIKAN: Menambahkan fungsi remove
    def remove(self, db: Session, *, id: int) -> User | None:
        """Menghapus pengguna berdasarkan ID."""
        obj = db.get(self.model, id)
        if obj:
            db.delete(obj)
            db.commit()
        return obj

    def increment_relationship_level(self, db: Session, *, db_obj: User, amount: int = 1) -> User:
        db_obj.relationship_level = (db_obj.relationship_level or 0) + amount
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

user = CRUDUser(User)
