from typing import Optional
from pydantic import BaseModel, Field

from .journal import Journal

class Article(BaseModel):
    title: str = Field(..., description="Article title")
    source: str = Field(..., description="Article source or publisher")
    image_url: str = Field(..., description="URL to an article image")

class FeedItem(BaseModel):
    type: str = Field(..., description="Type of feed item")
    journal: Optional[Journal] = Field(
        None, description="Journal entry associated with this feed"
    )
    article: Optional[Article] = Field(
        None, description="Article associated with this feed"
    )
    message: Optional[str] = Field(None, description="Optional feed message")
