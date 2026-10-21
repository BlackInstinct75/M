package com.example.chatai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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

data class Message(val id: String, val sender: String, val text: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                ChatScreen()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cabecera
        Text(
            text = "Mi Chat IA Nativo",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Lista de mensajes (equivalente al scroll de React)
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
                        color = if (isUser) MaterialTheme.colorScheme.primary else Color.LightGray,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isUser) Color.White else Color.Black,
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

        // Fila inferior de input y botón
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Escribe un mensaje...") },
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

                        // Llamada a tu API de FastAPI en segundo plano
                        coroutineScope.launch {
                            val reply = sendToFastAPI(userText)
                            val aiMsg = Message((System.currentTimeMillis() + 1).toString(), "assistant", reply)
                            messages = messages + aiMsg
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                Text("Enviar")
            }
        }
    }
}

// Función HTTP para conectar con FastAPI
suspend fun sendToFastAPI(message: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val json = JSONObject().put("message", message).toString()
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            
            // ⚠️ NOTA: Si usas el emulador de Android, 'localhost' se escribe como '10.0.2.2'
            val request = Request.Builder()
                .url("http://10.0.2.2:3000/api/chat")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext "Error del servidor"
                val responseBody = response.body?.string() ?: ""
                val jsonObject = JSONObject(responseBody)
                return@withContext jsonObject.optString("reply", "Respuesta vacía")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "⚠️ Error de conexión con FastAPI: ${e.localizedMessage}"
        }
    }
}