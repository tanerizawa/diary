# Lokasi: ./app/schemas/token.py
# Deskripsi: Skema Pydantic untuk data token JWT.

from pydantic import BaseModel, Field

class Token(BaseModel):
    access_token: str = Field(..., description="JWT access token")
    token_type: str = Field(..., description="Type of the token")

class TokenData(BaseModel):
    sub: str | None = Field(
        None, description="Subject identifier stored in the JWT"
    )  # 'sub' adalah nama standar untuk subjek di JWT
