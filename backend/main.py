import httpx
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# Permitir conexiones desde tu web y app móvil
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.post("/api/chat")
async def chat_with_local_ai(data: dict):
  user_message = data.get("message", "")

  # Petición al servidor local de Ollama
  async with httpx.AsyncClient() as client:
    try:
      response = await client.post(
          "http://localhost:11434/api/chat",
          json={
              "model": (  # Asegúrate de tener este modelo descargado (ej: ollama pull llama3)
                  "llama3"
              ),
              "messages": [{"role": "user", "content": user_message}],
              "stream": (  # False para recibir la respuesta completa de golpe
                  False
              ),
          },
          timeout=60.0,
      )

      if response.status_code != 200:
        return {"reply": "⚠️ Error al comunicarse con Ollama local."}

      result = response.json()
      # Extraemos la respuesta del modelo
      ai_reply = result.get("message", {}).get("content", "Sin respuesta")

      return {"reply": ai_reply}

    except Exception as e:
      print(f"Error de conexión: {e}")
      return {
          "reply": (
              "⚠️ No se pudo conectar con el servidor local de Ollama."
          )
      }