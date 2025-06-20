import hashlib
from typing import List
from sqlalchemy.orm import Session

from app import crud, models
from app.db.models.embedding import SemanticEmbedding


def generate_embedding(text: str) -> List[float]:
    """Generate a deterministic embedding vector for *text*."""
    digest = hashlib.sha256(text.encode("utf-8")).digest()
    # Use first 8 integers from digest as 32-bit chunks
    vector = [int.from_bytes(digest[i:i+4], "little") / 2**32 for i in range(0, 32, 4)]
    return vector


def cosine_similarity(a: List[float], b: List[float]) -> float:
    dot = sum(x * y for x, y in zip(a, b))
    norm_a = sum(x * x for x in a) ** 0.5
    norm_b = sum(x * x for x in b) ** 0.5
    if norm_a == 0 or norm_b == 0:
        return 0.0
    return dot / (norm_a * norm_b)


def find_similar_entries(db: Session, user: models.User, text: str, limit: int = 3) -> List[str]:
    """Return text of past messages or journals most similar to *text*."""
    target = generate_embedding(text)
    embeddings = db.query(SemanticEmbedding).filter(SemanticEmbedding.owner_id == user.id).all()
    scored: list[tuple[str, float]] = []
    for emb in embeddings:
        sim = cosine_similarity(target, emb.embedding)
        if emb.source_type == "chat":
            obj = crud.chat_message.get(db, id=emb.source_id)
            if obj:
                scored.append((obj.text, sim))
        else:
            obj = crud.journal.get(db, id=emb.source_id)
            if obj:
                scored.append((obj.content, sim))
    scored.sort(key=lambda x: x[1], reverse=True)
    return [t for t, _ in scored[:limit]]
