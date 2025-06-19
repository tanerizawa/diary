from pydantic import BaseModel

class EmotionLogBase(BaseModel):
    timestamp: int
    detected_mood: str | None = None
    source_text: str
    source_feature: str

class EmotionLogCreate(EmotionLogBase):
    pass

class EmotionLogUpdate(EmotionLogBase):
    pass

class EmotionLog(EmotionLogBase):
    id: int
    user_id: int

    class Config:
        from_attributes = True
