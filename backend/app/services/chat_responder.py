import os
from openai import OpenAI
from app.core.config import settings

# Inisialisasi client OpenAI dengan base_url OpenRouter
client = OpenAI(
    api_key=settings.AI_API_KEY,
    base_url="https://openrouter.ai/api/v1"
)

async def get_ai_reply(message: str, context: str = "") -> str | None:
    """Send a chat message to the OpenRouter API and return the reply."""
    system_prompt = (
        context if context else "You are a helpful mental health assistant."
    )
    try:
        # extra_headers untuk leaderboard OpenRouter (opsional)
        extra_headers = {
            "HTTP-Referer": "https://github.com/tanerizawa/diary",  # Ganti dengan URL Anda
            "X-Title": "Dear Diary App"                              # Ganti dengan nama aplikasi Anda
        }

        completion = client.chat.completions.create(
            model="google/gemini-flash-1.5",        # Ganti model sesuai kebutuhan
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": message}
            ],
            extra_headers=extra_headers
        )
        return completion.choices[0].message.content.strip()
    except Exception as e:
        print(f"Error calling AI service: {e}")
        return None