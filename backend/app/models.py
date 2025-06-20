# Lokasi: ./app/models.py
# Deskripsi: File ini bisa menjadi shortcut untuk mengimpor semua model,
# meskipun praktik yang lebih baik adalah mengimpor langsung dari app.db.models.

from app.db.models.user import User
from app.db.models.journal import JournalEntry
from app.db.models.chat import ChatMessage
from app.db.models.emotion_log import EmotionLog
