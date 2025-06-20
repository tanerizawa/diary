from pydantic import BaseModel, Field

class ConversationPlan(BaseModel):
    """Technique chosen for guiding the next assistant reply."""
    technique: str = Field(..., description="Conversation technique to apply")
