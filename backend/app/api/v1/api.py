# Lokasi: ./app/api/v1/api.py
# Deskripsi: Router utama yang menggabungkan semua endpoint dari versi v1.

from fastapi import APIRouter
from app.api.v1.endpoints import auth, journal
from app.api.v1.endpoints import chat

api_router = APIRouter()
api_router.include_router(auth.router, prefix="/users", tags=["auth"])
api_router.include_router(journal.router, prefix="/journal", tags=["journal"])
api_router.include_router(chat.router, prefix="/chat", tags=["chat"])
