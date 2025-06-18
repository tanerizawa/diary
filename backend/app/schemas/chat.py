from pydantic import BaseModel

class ChatRequest(BaseModel):
    message: str

class ChatResponse(BaseModel):
    reply: str
    sentiment_score: float | None = None
    key_emotions: str | None = None
