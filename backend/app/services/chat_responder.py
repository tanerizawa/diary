import httpx
import json
from app.core.config import settings
from app.schemas.action import Action

MAX_REPLY_LENGTH = 280

async def get_ai_reply(message: str, context: str = "", relationship_level: int = 0) -> dict | None:
    """
    Mengirim pesan ke OpenRouter API dan mengembalikan balasan.
    Balasan dibatasi maksimal 280 karakter dan bersifat personal-supportif.
    """
    instructions = (
        "Jawablah dengan kalimat personal dan hangat dalam Bahasa Indonesia "
        "dengan format JSON berikut: "
        '{"action": "<balas_teks|suggest_breathing_exercise|open_journal_editor|show_crisis_contact>",' 
        ' "text_response": "<pesan singkat>"} '
        f"Panjang text_response maksimum {MAX_REPLY_LENGTH} karakter."
    )

    if relationship_level > 30:
        relation = "close confidant"
    elif relationship_level > 10:
        relation = "friend"
    else:
        relation = "acquaintance"

    base = f"Anda adalah {relation} pengguna dan pendamping kesehatan mental yang suportif. "
    system_prompt = (
        f"{context}\n{base}{instructions}" if context
        else base + instructions
    )

    if not settings.AI_API_KEY or settings.AI_API_KEY == "CHANGE_ME":
        print("AI_API_KEY environment variable is not set or is a placeholder.")
        return None

    try:
        headers = {
            "Authorization": f"Bearer {settings.AI_API_KEY}",
            "HTTP-Referer": "https://github.com/tanerizawa/diary",  # Ganti jika perlu
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

        try:
            parsed = json.loads(reply)
            action = parsed.get("action", Action.balas_teks.value)
            text = parsed.get("text_response", "")
            journal_template = (
                parsed.get("journal_template")
                if action == Action.open_journal_editor.value
                else None
            )
        except json.JSONDecodeError:
            action = Action.balas_teks.value
            text = reply
            journal_template = None

        if len(text) > MAX_REPLY_LENGTH:
            text = text[:MAX_REPLY_LENGTH]

        result = {"action": action, "text_response": text}
        if journal_template is not None:
            result["journal_template"] = journal_template
        return result

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
