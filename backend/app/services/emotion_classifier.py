"""Simple text-based mood classifier returning an emoji."""

import re
from typing import Iterable

NEUTRAL_EMOJI = "\U0001F610"

# Keyword groups for heuristic classification
HAPPY_KEYWORDS: Iterable[str] = [
    "bahagia", "gembira", "excited", "fantastic", "wonderful", "luar biasa",
    "sangat senang", "thrilled", "delighted", "amazing"
]
POSITIVE_KEYWORDS: Iterable[str] = [
    "senang", "happy", "good", "great", "oke", "ok", "nice", "pleased", "love",
    "enjoy"
]
SAD_KEYWORDS: Iterable[str] = [
    "sedih", "sad", "down", "unhappy", "depressed", "kecewa", "murung",
    "miserable", "gloomy", "bad"
]
ANGRY_KEYWORDS: Iterable[str] = [
    "marah", "angry", "furious", "kesal", "jengkel", "annoyed", "irritated",
    "frustrated", "geram"
]


def _contains_keywords(text: str, keywords: Iterable[str]) -> bool:
    lower = text.lower()
    return any(re.search(r"\b" + re.escape(word) + r"\b", lower) for word in keywords)


def detect_mood(text: str) -> str:
    """Return an emoji describing the mood found in *text*."""
    if _contains_keywords(text, HAPPY_KEYWORDS):
        return "\U0001F600"  # 😀 very happy
    if _contains_keywords(text, POSITIVE_KEYWORDS):
        return "\U0001F642"  # 🙂 happy
    if _contains_keywords(text, ANGRY_KEYWORDS):
        return "\U0001F621"  # 😡 angry
    if _contains_keywords(text, SAD_KEYWORDS):
        return "\U0001F622"  # 😢 sad
    return NEUTRAL_EMOJI
