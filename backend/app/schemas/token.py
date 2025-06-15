# Lokasi: ./app/schemas/token.py
# Deskripsi: Skema Pydantic untuk data token JWT.

from pydantic import BaseModel

class Token(BaseModel):
    access_token: str
    token_type: str

class TokenData(BaseModel):
    sub: str | None = None # 'sub' adalah nama standar untuk subjek di JWT
