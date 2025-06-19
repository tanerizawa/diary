import httpx
import json
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

    if not settings.AI_API_KEY or settings.AI_API_KEY == "CHANGE_ME":
        print("AI_API_KEY environment variable is not set or is a placeholder.")
        return None

    try:
        headers = {
            "Authorization": f"Bearer {settings.AI_API_KEY}",
            "Referer": "https://github.com/tanerizawa/diary",  # Ganti jika perlu
            "X-Title": "Dear Diary App",
            "Content-Type": "application/json"
        }
        # Model diganti sesuai dengan yang ada di .env Anda
        body = {
            "model": settings.AI_MODEL,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": message}
            ]
        }

        # Inisialisasi response di luar blok async with agar bisa diakses di except block
        response = None
        async with httpx.AsyncClient() as client:
            response = await client.post(
                url=settings.AI_API_URL,
                headers=headers,
                json=body,
                timeout=30.0
            )
            # Ini akan otomatis memunculkan HTTPStatusError jika status code 4xx atau 5xx
            response.raise_for_status()

            data = response.json()
            reply = data["choices"][0]["message"]["content"].strip()

        # Potong jika melebihi batas karakter
        if len(reply) > MAX_REPLY_LENGTH:
            reply = reply[:MAX_REPLY_LENGTH]

        return reply

    # --- BLOK EXCEPT YANG DISEMPURNAKAN ---

    except httpx.HTTPStatusError as e:
        # Error ini terjadi jika API merespons dengan status 4xx atau 5xx (misal: 401 Unauthorized, 429 Rate Limit)
        print(f"AI service returned an error status: {e.response.status_code}")
        print(f"Response body: {e.response.text}") # Log ini akan menunjukkan pesan error dari AI
        return None

    except json.JSONDecodeError as e:
        # Error ini terjadi jika respons dari AI bukan JSON yang valid (misal: respons kosong atau halaman error HTML)
        print(f"Failed to decode JSON response from AI service: {e}")
        if response:
            print(f"Raw response content: {response.text}")
        return None

    except httpx.RequestError as e:
        # Error ini terjadi karena masalah jaringan (misal: timeout, tidak bisa terhubung)
        print(f"A network error occurred while calling AI service: {e}")
        return None

    except Exception as e:
        # Menangkap semua error tak terduga lainnya
        print(f"An unexpected error occurred in get_ai_reply: {e}")
        return None
