# M

Sistema de chat con Inteligencia Artificial estructurado bajo una arquitectura de monorepo moderno. Integra un backend de alto rendimiento, una interfaz web fluida y una aplicación móvil nativa.

---

## 🛠️ Stack Tecnológico

* **Frontend Web:** [Vite](https://vitejs.dev/) + [React](https://react.dev/) + TypeScript
* **Backend:** [Python](https://www.python.org/) + [FastAPI](https://fastapi.tiangolo.com/) + SDK de Google GenAI (`google-genai`)
* **App Móvil:** [Kotlin](https://kotlinlang.org/) + Android Native (Jetpack Compose)

---

## 📂 Estructura del Proyecto

```text
.
├── android-app/          # Aplicación nativa Android (Kotlin / Jetpack Compose)
├── backend/              # Servidor API REST (FastAPI y lógica de IA)
├── frontend-web/         # Aplicación de interfaz web (React + Vite)
└── README.md             # Documentación principal del monorepo