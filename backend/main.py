from fastapi import FastAPI
from app.api.v1.api import api_router
from app.core.config import settings
# Database schema now managed via Alembic migrations

app = FastAPI(title=settings.PROJECT_NAME)

app.include_router(api_router, prefix=settings.API_V1_STR)

@app.get("/")
def read_root():
    """Endpoint root untuk verifikasi bahwa server berjalan."""
    return {"message": f"Welcome to {settings.PROJECT_NAME}"}
