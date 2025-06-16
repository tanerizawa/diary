# Lokasi: ./app/db/base_class.py
# Deskripsi: Kelas dasar deklaratif untuk model SQLAlchemy.

from sqlalchemy.orm import as_declarative, declared_attr

@as_declarative()
class Base:
    id: int
    __name__: str

    # Membuat nama tabel secara otomatis dari nama kelas (misal: User -> users)
    @declared_attr
    def __tablename__(cls) -> str:
        return cls.__name__.lower() + "s"
