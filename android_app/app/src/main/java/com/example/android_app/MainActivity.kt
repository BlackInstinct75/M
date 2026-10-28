package com.example.android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
            // Esquema de colores estilo Dr. Doom / Latveria (Verde esmeralda y negro profundo)
            val doomColors = darkColorScheme(
                primary = Color(0xFF10B981),        // Verde esmeralda principal
                background = Color(0xFF040D0A),     // Negro verdoso profundo
                surface = Color(0xFF061A12),        // Superficies de contenedores
                onBackground = Color(0xFFECFDF5),   // Texto principal claro
                onSurface = Color(0xFFECFDF5)
            )

            MaterialTheme(colorScheme = doomColors) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF040D0A))
            .systemBarsPadding()
            .imePadding()
            .padding(16.dp)
    ) {
        // Cabecera estilo Latveria / Dr. Doom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "❖", fontSize = 20.sp, color = Color(0xFF10B981))
                Text(
                    text = "M_CHAT // PROTOCOL",
                    fontSize = 16.sp,
                    color = Color(0xFFECFDF5),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF062E22),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF059669))
            ) {
                Text(
                    text = "SECURE",
                    fontSize = 10.sp,
                    color = Color(0xFF34D399),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Línea divisoria de la cabecera
        Spacer(modifier = Modifier.height(4.dp))
        Divider(color = Color(0xFF064E3B), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Lista de mensajes con scroll
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        // Verde oscuro para ti, gris verdoso oscuro de búnker para la IA
                        color = if (isUser) Color(0xFF064E3B) else Color(0xFF0A1A14),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            if (isUser) Color(0xFF059669) else Color(0xFF133E2E)
                        ),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "USUARIO" else "AI",
                                fontSize = 10.sp,
                                color = if (isUser) Color(0xFFA7F3D0) else Color(0xFF6B7280),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = msg.text,
                                color = if (isUser) Color(0xFFECFDF5) else Color(0xFFD1D5DB),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            // Indicador de carga con puntos animados integrados en el chat
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0A1A14),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF133E2E))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "DOOM_AI:", fontSize = 10.sp, color = Color(0xFF6B7280))
                                TypingIndicator()
                            }
                        }
                    }
                }
            }
        }

        // Fila inferior fija de input y botón
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Introduce tu comando...", color = Color(0xFF6B7280)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF064E3B),
                    focusedTextColor = Color(0xFFECFDF5),
                    unfocusedTextColor = Color(0xFFECFDF5),
                    cursorColor = Color(0xFF10B981),
                    focusedContainerColor = Color(0xFF061A12),
                    unfocusedContainerColor = Color(0xFF061A12)
                ),
                shape = RoundedCornerShape(6.dp),
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = Color(0xFF064E3B)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text(
                    text = "ENVIAR",
                    color = Color(0xFF040D0A),
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Componente visual para la animación de los 3 puntos suspensivos
@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(index * 200)
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF10B981).copy(alpha = alpha), shape = CircleShape)
            )
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