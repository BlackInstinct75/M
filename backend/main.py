from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import os

app = FastAPI()

# 1. Configurar CORS para permitir que tu frontend web (corriendo en Vite) se comunique con FastAPI
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],  # Puerto por defecto de Vite
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 2. Definir el modelo de datos que esperas recibir desde la web (usando Pydantic)
class ChatRequest(BaseModel):
    message: str

@app.post("/api/chat")
async def chat_endpoint(request: ChatRequest):
    try:
        user_message = request.message
        print(f"Mensaje recibido del usuario: {user_message}")

        # --- AQUÍ LLAMARÍAS A LA API DE OPENAI / CLAUDE / ETC. ---
        # Ejemplo simulado de respuesta (reemplaza esto con tu lógica de IA real):
        ai_response = f"Hola desde FastAPI. He recibido tu mensaje: '{user_message}'"

        # FastAPI convierte automáticamente este diccionario en un JSON con la propiedad .reply
        return {"reply": ai_response}

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))