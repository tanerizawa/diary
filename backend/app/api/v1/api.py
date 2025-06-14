# Lokasi: ./app/api/v1/api.py
# Deskripsi: Router utama yang menggabungkan semua endpoint dari versi v1.

from fastapi import APIRouter
from app.api.v1.endpoints import auth, journal

api_router = APIRouter()
api_router.include_router(auth.router, prefix="/users", tags=["auth"])
api_router.include_router(journal.router, prefix="/journal", tags=["journal"])
