# Lokasi: ./app/schemas/user.py
# Deskripsi: Skema Pydantic untuk validasi data request/response pengguna.

from pydantic import BaseModel, EmailStr

# PERBAIKAN: Menambahkan skema untuk body request login
class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class UserBase(BaseModel):
    email: EmailStr

class UserCreate(UserBase):
    password: str

class User(UserBase):
    id: int
    is_active: bool

    # Config untuk kompatibilitas dengan ORM
    class Config:
        from_attributes = True
