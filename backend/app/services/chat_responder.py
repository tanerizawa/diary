import os
from openai import OpenAI
from app.core.config import settings

MAX_REPLY_LENGTH = 280

# Inisialisasi client OpenAI dengan base_url OpenRouter
client = OpenAI(
    api_key=settings.AI_API_KEY,
    base_url="https://openrouter.ai/api/v1"
)

async def get_ai_reply(message: str, context: str = "") -> str | None:
    """
    Mengirim pesan ke OpenRouter API dan mengembalikan balasan.
    Balasan dibatasi maksimal 280 karakter dan bersifat personal-supportif.
    """
    instructions = (
        "Jawablah dengan kalimat personal dan hangat dalam Bahasa Indonesia. "
        "Panjang jawaban maksimum 280 karakter."
    )

    system_prompt = (
        f"{context}\n{instructions}" if context
        else "Anda adalah pendamping kesehatan mental yang suportif. " + instructions
    )

    try:
        # Header tambahan untuk leaderboard OpenRouter (opsional)
        extra_headers = {
            "HTTP-Referer": "https://github.com/tanerizawa/diary",  # Ganti jika perlu
            "X-Title": "Dear Diary App"
        }

        completion = client.chat.completions.create(
            model="google/gemini-flash-1.5",  # Ganti model jika perlu
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": message}
            ],
            extra_headers=extra_headers
        )

        reply = completion.choices[0].message.content.strip()

        # Potong jika melebihi batas karakter
        if len(reply) > MAX_REPLY_LENGTH:
            reply = reply[:MAX_REPLY_LENGTH]

        return reply

    except Exception as e:
        print(f"Error calling AI service: {e}")
        return None
