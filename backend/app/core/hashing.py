# File: app/core/hashing.py
# Deskripsi: Berisi utilitas untuk hashing dan verifikasi password.

from passlib.context import CryptContext

# Konteks untuk hashing password
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Memverifikasi password yang dimasukkan dengan hash di database."""
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str) -> str:
    """Menghasilkan hash dari password."""
    return pwd_context.hash(password)