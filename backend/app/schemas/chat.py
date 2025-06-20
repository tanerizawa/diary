from pydantic import BaseModel, Field
from .action import Action

class ChatRequest(BaseModel):
    message: str = Field(..., description="Message from the user")

class ChatResponse(BaseModel):
    message_id: int = Field(..., description="ID of the created message")
    ai_message_id: int | None = Field(
        None, description="ID of the AI-generated reply message"
    )
    action: Action = Field(..., description="Action requested by the assistant")
    text_response: str = Field(..., description="Assistant reply text")
    sentiment_score: float | None = Field(
        None, description="Sentiment score for the reply"
    )
    key_emotions: str | None = Field(
        None, description="Key emotions detected in the conversation"
    )
    detected_mood: str | None = Field(
        None, description="Overall mood detected from the conversation"
    )
