# Lokasi: ./app/api/v1/api.py
# Deskripsi: Router utama yang menggabungkan semua endpoint dari versi v1.

from fastapi import APIRouter
from app.api.v1.endpoints import auth, journal
from app.api.v1.endpoints import chat, feed, emotion_log

api_router = APIRouter()
api_router.include_router(auth.router, prefix="/users", tags=["auth"])
api_router.include_router(journal.router, prefix="/journal", tags=["journal"])
api_router.include_router(chat.router, prefix="/chat", tags=["chat"])
api_router.include_router(feed.router, prefix="/feed", tags=["feed"])
api_router.include_router(emotion_log.router, prefix="/emotion", tags=["emotion"])
