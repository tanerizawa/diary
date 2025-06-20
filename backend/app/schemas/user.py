# Lokasi: ./app/schemas/user.py
# Deskripsi: Skema Pydantic untuk validasi data request/response pengguna.

from pydantic import BaseModel, EmailStr, Field, ConfigDict

# PERBAIKAN: Menambahkan skema untuk body request login
class LoginRequest(BaseModel):
    email: EmailStr = Field(..., description="User email for login")
    password: str = Field(..., description="Password for login")

class UserBase(BaseModel):
    email: EmailStr = Field(..., description="User email address")
    name: str | None = Field(None, description="Display name")
    bio: str | None = Field(None, description="Short biography")
    mbti_type: str | None = Field(None, description="MBTI personality type")

class UserCreate(UserBase):
    password: str = Field(..., description="Plaintext password for the new user")

class UserUpdate(BaseModel):
    name: str | None = Field(None, description="Updated display name")
    bio: str | None = Field(None, description="Updated biography")
    relationship_level: int | None = Field(
        None, description="Updated relationship level with the assistant"
    )
    mbti_type: str | None = Field(None, description="Updated MBTI type")

class UserMBTIUpdate(BaseModel):
    mbti_type: str = Field(..., description="New MBTI type")

class User(UserBase):
    id: int = Field(..., description="Unique user identifier")
    is_active: bool = Field(..., description="Indicates if the user is active")
    relationship_level: int = Field(
        ..., description="Current relationship level with the assistant"
    )

    # Config untuk kompatibilitas dengan ORM
    model_config = ConfigDict(from_attributes=True)
