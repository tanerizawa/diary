from pydantic import BaseModel, Field

class EmotionLogBase(BaseModel):
    timestamp: int = Field(..., description="Unix timestamp for the log")
    detected_mood: str | None = Field(
        None, description="Mood detected from the source text"
    )
    source_text: str = Field(..., description="Original text analyzed")
    source_feature: str = Field(..., description="Feature or context of the text")
    sentiment_score: float | None = Field(
        None, description="Sentiment score of the source text"
    )
    key_emotions_detected: list[str] | None = Field(
        None, description="List of key emotions extracted"
    )

class EmotionLogCreate(EmotionLogBase):
    pass

class EmotionLogUpdate(EmotionLogBase):
    pass

class EmotionLog(EmotionLogBase):
    id: int = Field(..., description="Unique emotion log identifier")
    user_id: int = Field(..., description="Identifier of the related user")

    class Config:
        from_attributes = True
