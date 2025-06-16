# File BARU: backend/app/services/sentiment_analyzer.py

import httpx
from app.core.config import settings
import json

async def analyze_sentiment_with_ai(text: str) -> dict | None:
    """
    Mengirim teks jurnal ke layanan AI untuk analisis sentimen
    dan ekstraksi emosi kunci.
    """
    # Prompt yang akan kita kirim ke model AI
    prompt = f"""
    Analyze the sentiment of the following journal entry.
    Provide your response in a raw JSON format, without any surrounding text or markdown.
    The JSON object must have two keys:
    1. "sentiment_score": a float value between -1.0 (very negative) and 1.0 (very positive).
    2. "key_emotions": a string containing a comma-separated list of 1 to 3 key emotions found in the text (e.g., "happy,grateful,optimistic").

    Journal Entry:
    "{text}"
    """

    headers = {
        "Authorization": f"Bearer {settings.AI_API_KEY}",
        "Content-Type": "application/json"
    }

    # Model yang akan digunakan (contoh: Google Gemini Flash)
    # Anda bisa menggantinya dengan model lain yang tersedia di OpenRouter
    body = {
        "model": "google/gemini-flash-1.5",
        "messages": [
            {"role": "user", "content": prompt}
        ],
        "response_format": {"type": "json_object"} # Meminta output JSON
    }

    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                url=settings.AI_API_URL,
                headers=headers,
                json=body,
                timeout=30.0 # Timeout 30 detik
            )
            response.raise_for_status() # Lemparkan error jika status code bukan 2xx

            # Parsing respons JSON dari AI
            ai_response = response.json()
            content_str = ai_response["choices"][0]["message"]["content"]

            # Mengubah string JSON di dalam content menjadi dictionary Python
            result_data = json.loads(content_str)

            return {
                "sentiment_score": result_data.get("sentiment_score"),
                "key_emotions": result_data.get("key_emotions")
            }

    except (httpx.RequestError, httpx.HTTPStatusError, json.JSONDecodeError, KeyError) as e:
        print(f"Error calling AI service: {e}")
        return None
