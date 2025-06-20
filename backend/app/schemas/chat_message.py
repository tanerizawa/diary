from pydantic import BaseModel, Field, ConfigDict

class ChatMessageBase(BaseModel):
    text: str = Field(..., description="Chat message text")
    is_user: bool = Field(..., description="Indicates if sender is the user")
    timestamp: int = Field(..., description="Unix timestamp of the message")
    sentiment_score: float | None = Field(
        None, description="Sentiment score for the message"
    )
    key_emotions: str | None = Field(
        None, description="Key emotions extracted from the message"
    )
    detected_mood: str | None = Field(
        None, description="Detected mood from the message"
    )

class ChatMessageCreate(ChatMessageBase):
    pass

class ChatMessageUpdate(ChatMessageBase):
    pass

class ChatMessage(ChatMessageBase):
    id: int = Field(..., description="Unique message identifier")
    owner_id: int = Field(..., description="Identifier of the message owner")

    model_config = ConfigDict(from_attributes=True)


class ChatMessageDeleteRequest(BaseModel):
    """Payload for deleting one or more chat messages."""
    ids: list[int] = Field(..., description="IDs of messages to delete")
