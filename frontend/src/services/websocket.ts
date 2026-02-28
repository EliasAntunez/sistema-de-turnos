import { Client } from '@stomp/stompjs'
import type { Frame, IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export interface NotificacionWebSocket {
  id: number
  tipo: string
  titulo: string
  mensaje: string
  contenidoJson?: string
  turnoId?: number
  fechaCreacion: string
}

export type NotificacionCallback = (notificacion: NotificacionWebSocket) => void

/**
 * Servicio de WebSocket para notificaciones en tiempo real
 * Usa STOMP sobre SockJS para comunicación bidireccional
 */
class WebSocketService {
  private client: Client | null = null
  private connected: boolean = false
  private reconnectAttempts: number = 0
  private maxReconnectAttempts: number = 5
  private reconnectDelay: number = 3000
  private callback: NotificacionCallback | null = null // M3: Callback único en lugar de array
  private profesionalId: number | null = null
  private reconnectTimeoutId: number | null = null
  private manualDisconnect: boolean = false

  /**
   * Inicializar conexión WebSocket
   * @param profesionalId ID del profesional autenticado
   * @param onNotification Callback para manejar notificaciones recibidas
   */
  connect(profesionalId: number, onNotification: NotificacionCallback) {
    if (this.connected) {
      if (import.meta.env.DEV) console.warn('WebSocket ya está conectado')
      return
    }

    this.profesionalId = profesionalId
    this.callback = onNotification // M3: Asignación directa en lugar de push a array
    this.manualDisconnect = false

    this.createClient()
  }

  /**
   * Crear y activar el cliente STOMP
   */
  private createClient() {
    // Obtener URL del WebSocket desde variables de entorno (C4)
    const wsUrl = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws'
    
    if (import.meta.env.DEV) console.log('🔌 Conectando a WebSocket:', wsUrl)

    // Crear cliente STOMP
    this.client = new Client({
      // Usar SockJS como transporte (fallback si WebSocket no está disponible)
      webSocketFactory: () => {
        return new SockJS(wsUrl) as any
      },
      
      // Configuración de reconexión automática
      reconnectDelay: this.reconnectDelay,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      // Callbacks
      onConnect: (frame: Frame) => {
        this.onConnected(frame)
      },
      
      onStompError: (frame: Frame) => {
        console.error('❌ Error STOMP:', frame.headers['message'], frame.body)
      },
      
      onWebSocketError: (event: Event) => {
        console.error('❌ Error WebSocket:', event)
      },
      
      onDisconnect: () => {
        this.onDisconnected()
      }
    })

    // Activar cliente
    this.client.activate()
  }

  /**
   * Callback cuando se establece la conexión
   */
  private onConnected(frame: Frame) {
    this.connected = true
    this.reconnectAttempts = 0
    
    if (import.meta.env.DEV) console.log('✅ WebSocket conectado exitosamente')

    // Suscribirse al canal de notificaciones del profesional
    const topic = `/topic/notifications/${this.profesionalId}`
    
    if (this.client) {
      this.client.subscribe(topic, (message: IMessage) => {
        try {
          const notificacion: NotificacionWebSocket = JSON.parse(message.body)
          
          if (import.meta.env.DEV) console.log('📬 Nueva notificación recibida:', notificacion.tipo)
          
          // M3: Llamar al callback único
          if (this.callback) {
            this.callback(notificacion)
          }
        } catch (error) {
          console.error('❌ Error al parsear notificación:', error)
        }
      })
      
      if (import.meta.env.DEV) console.log('📡 Suscrito al canal:', topic)
    }
  }

  /**
   * Callback cuando se pierde la conexión (C5 - Reconexión automática)
   */
  private onDisconnected() {
    this.connected = false

    // Si fue desconexión manual, no intentar reconectar
    if (this.manualDisconnect) {
      if (import.meta.env.DEV) console.log('👋 WebSocket desconectado manualmente')
      return
    }

    // Intentar reconectar automáticamente con backoff exponencial
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      
      // Calcular delay con backoff exponencial: 3s, 6s, 12s, 24s, 48s
      const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1)
      
      if (import.meta.env.DEV) {
        console.warn(
          `⚠️ WebSocket desconectado. Reintentando en ${delay / 1000}s... (Intento ${this.reconnectAttempts}/${this.maxReconnectAttempts})`
        )
      }

      // Limpiar timeout anterior si existe
      if (this.reconnectTimeoutId !== null) {
        window.clearTimeout(this.reconnectTimeoutId)
      }

      // Programar reconexión
      this.reconnectTimeoutId = window.setTimeout(() => {
        if (this.profesionalId !== null && !this.manualDisconnect) {
          if (import.meta.env.DEV) console.log('🔄 Intentando reconectar...')
          this.createClient()
        }
      }, delay)
      
      // Notificar al usuario (opcional - puedes emitir evento)
      window.dispatchEvent(new CustomEvent('websocket-reconnecting', { 
        detail: { 
          attempt: this.reconnectAttempts, 
          maxAttempts: this.maxReconnectAttempts,
          nextRetryDelay: delay
        } 
      }))
    } else {
      console.error(
        `❌ No se pudo reconectar después de ${this.maxReconnectAttempts} intentos. WebSocket desconectado permanentemente.`
      )
      
      // Notificar fallo permanente
      window.dispatchEvent(new CustomEvent('websocket-disconnected-permanently'))
    }
  }

  /**
   * Desconectar WebSocket
   */
  disconnect() {
    if (import.meta.env.DEV) console.log('👋 Desconectando WebSocket...')
    
    this.manualDisconnect = true
    
    // Limpiar timeout de reconexión
    if (this.reconnectTimeoutId !== null) {
      window.clearTimeout(this.reconnectTimeoutId)
      this.reconnectTimeoutId = null
    }
    
    if (this.client) {
      this.client.deactivate()
      this.client = null
    }
    
    this.connected = false
    this.callback = null // M3: Limpiar callback único
    this.profesionalId = null
    this.reconnectAttempts = 0
  }

  /**
   * Verificar si está conectado
   */
  isConnected(): boolean {
    return this.connected
  }
  
  /**
   * Obtener número de intentos de reconexión actuales
   */
  getReconnectAttempts(): number {
    return this.reconnectAttempts
  }
  
  /**
   * Resetear manualmente el contador de reconexiones
   */
  resetReconnectAttempts(): void {
    this.reconnectAttempts = 0
  }
}

// Exportar instancia singleton
export const webSocketService = new WebSocketService()
