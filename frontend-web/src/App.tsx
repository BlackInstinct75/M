import { useState, useRef, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import './App.css';

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

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isLoading]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMessageText = input;
    const newUserMessage: Message = {
      id: Date.now().toString(),
      sender: 'user',
      text: userMessageText,
    };

    setMessages((prev) => [...prev, newUserMessage]);
    setInput('');
    setIsLoading(true);

    try {
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
      width: '100%',
      maxWidth: '700px',
      margin: '0 auto', 
      padding: '20px', 
      boxSizing: 'border-box',  
      fontFamily: 'sans-serif',
      backgroundColor: '#07110d', // Fondo general estilo Doom (verde muy oscuro/negruzco)
      color: '#e2e8f0'
    }}>
      
      {/* Cabecera estilo Latveria / Dr. Doom */}
      <header style={{ 
        borderBottom: '1px solid #143828', 
        paddingBottom: '12px', 
        marginBottom: '20px', 
        flexShrink: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}>
        <h2 style={{ margin: 0, color: '#10b981', letterSpacing: '2px', fontFamily: 'monospace' }}>M // LATVERIAN_AI</h2>
        <span style={{ fontSize: '12px', color: '#047857', border: '1px solid #064e3b', padding: '2px 8px', borderRadius: '4px' }}>SECURE_NET</span>
      </header>

      {/* Ventana de mensajes con scroll integrado */}
      <div style={{ 
        flex: 1, 
        minHeight: 0,          
        overflowY: 'auto', 
        display: 'flex', 
        flexDirection: 'column', 
        gap: '14px', 
        paddingRight: '10px' 
      }}>
        {messages.length === 0 && (
          <div style={{ textAlign: 'center', marginTop: '60px', color: '#4b5563' }}>
            <p style={{ fontFamily: 'monospace', fontSize: '14px' }}>[ SISTEMA LISTO. ESTABLECIENDO PROTOCOLO DE CONVERSACIÓN ]</p>
          </div>
        )}

        {messages.map((msg) => (
          <div
            key={msg.id}
            style={{
              alignSelf: msg.sender === 'user' ? 'flex-end' : 'flex-start',
              backgroundColor: msg.sender === 'user' ? '#065f46' : '#111827',
              color: msg.sender === 'user' ? '#f0fdf4' : '#d1d5db',
              padding: '12px 16px',
              borderRadius: '8px',
              maxWidth: '75%',
              wordBreak: 'break-word',
              border: msg.sender === 'user' ? '1px solid #059669' : '1px solid #1f2937',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.2)'
            }}
          >
            <div style={{ fontSize: '11px', color: msg.sender === 'user' ? '#a7f3d0' : '#6b7280', marginBottom: '4px', fontWeight: 'bold', fontFamily: 'monospace' }}>
              {msg.sender === 'user' ? 'USUARIO' : 'AI'}
            </div>
            {msg.sender === 'assistant' ? (
              <div className="markdown">
                <ReactMarkdown>{msg.text}</ReactMarkdown>
              </div>
            ) : (
              <p style={{ margin: '0', whiteSpace: 'pre-wrap' }}>{msg.text}</p>
            )}
          </div>
        ))}

        {/* Burbuja de carga integrada como un mensaje más al final (sin solaparse) */}
        {isLoading && (
          <div style={{ 
            alignSelf: 'flex-start', 
            backgroundColor: '#111827', 
            padding: '12px 18px', 
            borderRadius: '8px', 
            border: '1px solid #1f2937',
            display: 'flex',
            alignItems: 'center',
            gap: '8px'
          }}>
            <span style={{ fontSize: '11px', color: '#6b7280', fontWeight: 'bold', fontFamily: 'monospace' }}>AI:</span>
            <div className="typing-dots" style={{ display: 'flex', gap: '4px', alignItems: 'center', height: '15px' }}>
              <span>•</span>
              <span>•</span>
              <span>•</span>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input inferior fijo */}
      <form onSubmit={handleSubmit} style={{ marginTop: '20px', flexShrink: 0 }}>
        <div style={{ position: 'relative', display: 'flex', alignItems: 'center', width: '100%' }}>
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Introduce tu comando..."
            style={{
              width: '100%',
              padding: '14px 100px 14px 16px',
              borderRadius: '6px',
              border: '1px solid #064e3b',
              fontSize: '15px',
              outline: 'none',
              backgroundColor: '#0a1812',
              color: '#f0fdf4',
              boxSizing: 'border-box'
            }}
            disabled={isLoading}
          />
          <button
            type="submit"
            disabled={isLoading || !input.trim()}
            style={{
              position: 'absolute',
              right: '6px',
              padding: '8px 16px',
              borderRadius: '4px',
              border: 'none',
              backgroundColor: '#059669',
              color: '#fff',
              cursor: 'pointer',
              fontWeight: 'bold',
              fontSize: '13px',
              fontFamily: 'monospace',
              letterSpacing: '1px'
            }}
          >
            ENVIAR
          </button>
        </div>
      </form>
    </div>
  );
}

export default App;