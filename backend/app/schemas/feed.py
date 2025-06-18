from typing import Optional
from pydantic import BaseModel

from .journal import Journal

class Article(BaseModel):
    title: str
    source: str
    image_url: str

class FeedItem(BaseModel):
    type: str
    journal: Optional[Journal] = None
    article: Optional[Article] = None
    message: Optional[str] = None
