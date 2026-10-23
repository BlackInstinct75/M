FROM python:3.11-slim

WORKDIR /app

# Instalar dependencias
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar el código del backend
COPY . .

EXPOSE 3000

# Arrancar FastAPI escuchando en todas las interfaces
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "3000"]