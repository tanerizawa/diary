from pydantic import BaseModel

class ChatMessageBase(BaseModel):
    text: str
    is_user: bool
    timestamp: int

class ChatMessageCreate(ChatMessageBase):
    pass

class ChatMessageUpdate(ChatMessageBase):
    pass

class ChatMessage(ChatMessageBase):
    id: int
    owner_id: int

    class Config:
        from_attributes = True
