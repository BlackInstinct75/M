import { useState, useRef, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import './App.css'; // O tus estilos si usas Tailwind

// Definimos la estructura de un mensaje
interface Message {
  id: string;
  sender: 'user' | 'assistant';
  text: string;
}

function App() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll hacia abajo cuando llega un mensaje nuevo
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  // Función que se ejecuta al enviar el formulario
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMessageText = input;
    const newUserMessage: Message = {
      id: Date.now().toString(),
      sender: 'user',
      text: userMessageText,
    };

    // 1. Añadimos tu mensaje a la pantalla
    setMessages((prev) => [...prev, newUserMessage]);
    setInput('');
    setIsLoading(true);

    try {
      // 2. AQUÍ CONECTAS CON TU BACKEND (Node.js)
      const response = await fetch('http://localhost:3000/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message: userMessageText }),
      });

      if (!response.ok) throw new Error('Error al conectar con el servidor');

      const data = await response.json();
      
      const aiMessage: Message = {
        id: (Date.now() + 1).toString(),
        sender: 'assistant',
        text: data.reply,
      };

      // 3. Añadimos la respuesta de la IA
      setMessages((prev) => [...prev, aiMessage]);

    } catch (error) {
      console.error('Error:', error);
      const errorMessage: Message = {
        id: (Date.now() + 1).toString(),
        sender: 'assistant',
        text: '⚠️ Hubo un error al conectar con el servidor o la API.',
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ 
      display: 'flex', 
      flexDirection: 'column', 
      height: '100vh',          
      width: '35%', 
      margin: '0 auto', 
      padding: '20px', 
      boxSizing: 'border-box',  
      fontFamily: 'sans-serif' 
    }}>
      
      {/* Cabecera */}
      <header style={{ borderBottom: '1px solid #ccc', paddingBottom: '10px', marginBottom: '20px', flexShrink: 0 }}>
        <h2>M</h2>
      </header>

      {/* Ventana de mensajes (con scroll y minHeight: 0 para que funcione el flex scroll) */}
      <div style={{ 
        flex: 1, 
        minHeight: 0,          
        overflowY: 'auto', 
        display: 'flex', 
        flexDirection: 'column', 
        gap: '12px', 
        paddingRight: '10px' 
      }}>
        {messages.length === 0 && (
          <p style={{ color: '#888', textAlign: 'center', marginTop: '40px' }}>
            Escríbeme lo que piensas
          </p>
        )}

        {messages.map((msg) => (
          <div
            key={msg.id}
            style={{
              alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
              backgroundColor: msg.sender === 'user' ? '#007bff' : '#f1f1f1',
              color: msg.sender === 'user' ? '#fff' : '#000',
              padding: '10px 15px',
              borderRadius: '12px',
              maxWidth: '70%',
              wordBreak: 'break-word',
            }}
          >
            <strong>{msg.sender === 'user' ? 'Tú' : 'IA'}:</strong>
            {msg.sender === 'assistant' ? (
              <div className="markdown">
                <ReactMarkdown>{msg.text}</ReactMarkdown>
              </div>
            ) : (
              <p style={{ margin: '5px 0 0 0', whiteSpace: 'pre-wrap' }}>{msg.text}</p>
            )}
          </div>
        ))}

        {isLoading && (
          <div style={{ alignSelf: 'flex-start', backgroundColor: '#f1f1f1', padding: '10px 15px', borderRadius: '12px', color: '#666' }}>
            <em>La IA está pensando...</em>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input inferior fijo (flexShrink: 0 evita que se aplaste) */}
      <form onSubmit={handleSubmit} style={{ marginTop: '20px', flexShrink: 0 }}>
        <div style={{ position: 'relative', display: 'flex', alignItems: 'center', width: '100%' }}>
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Escribe tu mensaje..."
            style={{
              width: '100%',
              padding: '14px 95px 14px 14px',
              borderRadius: '8px',
              border: '1px solid var(--border, #ccc)',
              fontSize: '16px',
              outline: 'none',
              backgroundColor: 'var(--bg, #fff)',
              color: 'var(--text-h, #000)',
              boxSizing: 'border-box'
            }}
            disabled={isLoading}
          />
          <button
            type="submit"
            disabled={isLoading || !input.trim()}
            style={{
              position: 'absolute',
              right: '8px',
              padding: '8px 16px',
              borderRadius: '6px',
              border: 'none',
              backgroundColor: 'var(--accent, #007bff)',
              color: '#fff',
              cursor: 'pointer',
              fontWeight: 'bold',
              fontSize: '14px'
            }}
          >
            Enviar
          </button>
        </div>
      </form>
    </div>
  );
}

export default App;