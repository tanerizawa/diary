# Lokasi: ./app/schemas/user.py
# Deskripsi: Skema Pydantic untuk validasi data request/response pengguna.

from pydantic import BaseModel, EmailStr

# PERBAIKAN: Menambahkan skema untuk body request login
class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class UserBase(BaseModel):
    email: EmailStr
    name: str | None = None
    bio: str | None = None
    mbti_type: str | None = None

class UserCreate(UserBase):
    password: str

class UserUpdate(BaseModel):
    name: str | None = None
    bio: str | None = None
    relationship_level: int | None = None
    mbti_type: str | None = None

class UserMBTIUpdate(BaseModel):
    mbti_type: str

class User(UserBase):
    id: int
    is_active: bool
    relationship_level: int

    # Config untuk kompatibilitas dengan ORM
    class Config:
        from_attributes = True
