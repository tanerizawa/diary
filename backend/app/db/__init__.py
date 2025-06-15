# Lokasi: ./app/db/__init__.py
# Deskripsi: Memudahkan impor dari modul database.

from .base_class import Base
from .models.user import User
from .models.journal import JournalEntry