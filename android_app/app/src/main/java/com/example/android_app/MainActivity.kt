package com.example.android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class Message(val id: String, val sender: String, val text: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Definimos un esquema de colores totalmente oscuro (negro)
            val darkColors = darkColorScheme(
                primary = Color(0xFF3B82F6),
                background = Color.Black,
                surface = Color(0xFF121212),
                onBackground = Color.White,
                onSurface = Color.White
            )

            MaterialTheme(colorScheme = darkColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen() {
    var messages by remember { mutableStateOf(listOf<Message>()) }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // systemBarsPadding protege la cámara y la barra inferior del sistema
    // imePadding empuja toda la interfaz hacia arriba cuando aparece el teclado
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(16.dp)
    ) {
        // Cabecera minimalista
        Text(
            text = "M",
            fontSize = 24.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Lista de mensajes con scroll
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        // Azul para ti, gris muy oscuro para la IA
                        color = if (isUser) Color(0xFF2563EB) else Color(0xFF27272A),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
            if (isLoading) {
                item {
                    Text(text = "La IA está pensando...", color = Color.Gray, fontSize = 14.sp)
                }
            }
        }

        // Fila inferior fija de input y botón
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Escribe un mensaje...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color(0xFF4B5563),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF3B82F6)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            Button(
                onClick = {
                    if (input.isNotBlank() && !isLoading) {
                        val userText = input
                        val userMsg = Message(System.currentTimeMillis().toString(), "user", userText)
                        messages = messages + userMsg
                        input = ""
                        isLoading = true

                        coroutineScope.launch {
                            val reply = sendToFastAPI(userText)
                            val aiMsg = Message((System.currentTimeMillis() + 1).toString(), "assistant", reply)
                            messages = messages + aiMsg
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Enviar", color = Color.White)
            }
        }
    }
}

// Función HTTP con timeout de 60s apuntando a tu red local
suspend fun sendToFastAPI(message: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            val json = JSONObject().put("message", message).toString()
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("http://192.168.0.49:3000/api/chat")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext "Error del servidor: ${response.code}"
                val responseBody = response.body?.string() ?: ""
                val jsonObject = JSONObject(responseBody)
                return@withContext jsonObject.optString("reply", "Respuesta vacía")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "⚠️ Error de timeout o conexión con FastAPI: ${e.localizedMessage}"
        }
    }
}