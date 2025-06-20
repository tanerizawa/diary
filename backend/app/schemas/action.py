from enum import Enum
from pydantic import BaseModel, Field

class Action(str, Enum):
    """Permissible actions returned by the AI assistant."""

    balas_teks = "balas_teks"
    suggest_breathing_exercise = "suggest_breathing_exercise"
    open_journal_editor = "open_journal_editor"
    show_crisis_contact = "show_crisis_contact"

class ActionResponse(BaseModel):
    action: Action = Field(..., description="Action type")
    text_response: str = Field(..., description="Textual response from assistant")
