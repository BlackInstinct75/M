import os
import traceback

import openai
from openai import OpenAI
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

load_dotenv()

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mismo SDK de OpenAI, apuntando a xAI. max_retries: reintentos automáticos ante 429/5xx
client = OpenAI(
    api_key=os.environ.get("XAI_API_KEY"),
    base_url="https://api.x.ai/v1",
    max_retries=3,
)

MODEL = os.environ.get("XAI_MODEL", "grok-4-6")

SYSTEM_PROMPT = "Eres un asistente útil. Responde en el idioma del usuario."


class ChatRequest(BaseModel):
    message: str


@app.post("/api/chat")
def chat_endpoint(body: ChatRequest):
    try:
        completion = client.chat.completions.create(
            model=MODEL,
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": body.message},
            ],
            max_tokens=2048,
        )
        reply = completion.choices[0].message.content or "Sin respuesta"
        return {"reply": reply.strip()}

    except openai.AuthenticationError:
        raise HTTPException(status_code=401, detail="Clave de xAI no válida. Revisa tu .env.")
    except openai.NotFoundError:
        raise HTTPException(status_code=404, detail=f"El modelo '{MODEL}' no existe o no tienes acceso.")
    except openai.RateLimitError as e:
        print("429 de xAI:", e)
        raise HTTPException(status_code=429, detail=str(e))
    except (openai.APIConnectionError, openai.InternalServerError):
        raise HTTPException(status_code=503, detail="xAI no está disponible ahora mismo. Inténtalo en unos segundos.")
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))