import httpx
from app.core.config import settings

MAX_REPLY_LENGTH = 280

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

    if not settings.AI_API_KEY:
        print("AI_API_KEY environment variable is not set")
        return None

    try:
        headers = {
            "Authorization": f"Bearer {settings.AI_API_KEY}",
            "Referer": "https://github.com/tanerizawa/diary",  # Ganti jika perlu
            "X-Title": "Dear Diary App",
            "Content-Type": "application/json"
        }
        body = {
            "model": "google/gemini-2.0-flash-001",
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": message}
            ]
        }

        async with httpx.AsyncClient() as client:
            response = await client.post(
                url=settings.AI_API_URL,
                headers=headers,
                json=body,
                timeout=30.0
            )
            response.raise_for_status()

            data = response.json()
            reply = data["choices"][0]["message"]["content"].strip()

        # Potong jika melebihi batas karakter
        if len(reply) > MAX_REPLY_LENGTH:
            reply = reply[:MAX_REPLY_LENGTH]

        return reply

    except Exception as e:
        print(f"Error calling AI service: {e}")
        return None
