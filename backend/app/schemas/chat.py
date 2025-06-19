from pydantic import BaseModel, Field

class ChatRequest(BaseModel):
    message: str = Field(..., description="Message from the user")

class ChatResponse(BaseModel):
    reply_text: str = Field(..., description="Assistant reply text")
    sentiment_score: float | None = Field(
        None, description="Sentiment score for the reply"
    )
    key_emotions: str | None = Field(
        None, description="Key emotions detected in the conversation"
    )
    detected_mood: str | None = Field(
        None, description="Overall mood detected from the conversation"
    )
