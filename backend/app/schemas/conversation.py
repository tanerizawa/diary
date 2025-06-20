from pydantic import BaseModel, Field

class ConversationPlan(BaseModel):
    """Technique chosen for guiding the next assistant reply."""
    technique: str = Field(..., description="Conversation technique to apply")

    @property
    def technique_to_use(self) -> str:
        """Alias property for the technique string."""
        return self.technique
