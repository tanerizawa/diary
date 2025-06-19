from pydantic import BaseModel

class ChatMessageBase(BaseModel):
    text: str
    is_user: bool
    timestamp: int
    sentiment_score: float | None = None
    key_emotions: str | None = None
    detected_mood: str | None = None

class ChatMessageCreate(ChatMessageBase):
    pass

class ChatMessageUpdate(ChatMessageBase):
    pass

class ChatMessage(ChatMessageBase):
    id: int
    owner_id: int

    class Config:
        from_attributes = True
