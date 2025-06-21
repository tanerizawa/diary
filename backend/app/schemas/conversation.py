from enum import Enum
from pydantic import BaseModel, Field


class CommunicationTechnique(str, Enum):
    """Enumeration of counselling techniques used by the assistant."""

    PROBING = "Probing"
    CLARIFYING = "Clarifying"
    PARAPHRASING = "Paraphrasing"
    REFLECTING = "Reflecting"
    OPEN_ENDED_QUESTIONS = "Open-ended questions"
    CLOSED_ENDED_QUESTIONS = "Closed-ended questions"
    SUMMARIZING = "Summarizing"
    CONFRONTATION = "Confrontation"
    REASSURANCE_ENCOURAGEMENT = "Reassurance and encouragement"
    NEUTRAL_ACKNOWLEDGEMENT = "Neutral acknowledgement"


class ConversationPlan(BaseModel):
    """Technique chosen for guiding the next assistant reply."""

    technique: CommunicationTechnique = Field(
        ..., description="Conversation technique to apply"
    )

    @property
    def technique_to_use(self) -> str:
        """Return the human readable technique name."""
        return self.technique.value
