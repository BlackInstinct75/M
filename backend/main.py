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

    # Petición al servidor local de Ollama (Usando /api/chat que admite "messages")
    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                "http://host.docker.internal:11434/api/chat",
                json={
                    "model": "llama3",  # Asegúrate de tenerlo con: ollama pull llama3
                    "messages": [{"role": "user", "content": user_message}],
                    "stream": False,  # False para recibir la respuesta completa de golpe
                },
                timeout=60.0,
            )

            if response.status_code != 200:
                return {"reply": f"⚠️ Error al comunicarse con Ollama: {response.text}"}

            result = response.json()
            
            # 💡 CORRECCIÓN CLAVE: En /api/chat la estructura es result["message"]["content"]
            ai_reply = result.get("message", {}).get("content", "Sin respuesta")

            return {"reply": ai_reply}

        except Exception as e:
            print(f"Error de conexión: {e}")
            return {
                "reply": "⚠️ No se pudo conectar con el servidor local de Ollama."
            }