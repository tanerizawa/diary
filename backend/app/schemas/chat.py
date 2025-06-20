from pydantic import BaseModel, Field

class ChatRequest(BaseModel):
    message: str = Field(..., description="Message from the user")

class FinalChatResponse(BaseModel):
    message_id: int = Field(..., description="ID of the created message")
    ai_message_id: int | None = Field(
        None, description="ID of the AI-generated reply message"
    )
    text_response: str = Field(..., description="Assistant reply text")
