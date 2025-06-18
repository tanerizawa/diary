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
        )
        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    # PERBAIKAN: Menambahkan fungsi remove
    def remove(self, db: Session, *, id: int) -> User | None:
        """Menghapus pengguna berdasarkan ID."""
        obj = db.query(self.model).get(id)
        if obj:
            db.delete(obj)
            db.commit()
        return obj

user = CRUDUser(User)
