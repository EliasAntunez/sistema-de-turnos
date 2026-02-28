import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { webSocketService, type NotificacionWebSocket } from '../services/websocket'
import { notificacionesService, type Notificacion } from '../services/notificaciones'
import { useAuthStore } from './auth'

export const useNotificacionesStore = defineStore('notificaciones', () => {
  // Estado
  const notificaciones = ref<Notificacion[]>([])
  const contadorNoLeidas = ref(0)
  const cargando = ref(false)
  const conectado = ref(false)

  // Computed
  const notificacionesNoLeidas = computed(() => 
    notificaciones.value.filter(n => !n.leida)
  )

  const tieneNotificacionesNoLeidas = computed(() => 
    contadorNoLeidas.value > 0
  )

  /**
   * Inicializar WebSocket y cargar notificaciones iniciales
   */
  async function inicializar() {
    const authStore = useAuthStore()
    
    if (!authStore.usuario || !authStore.usuario.id) {
      if (import.meta.env.DEV) console.warn('⚠️ No hay usuario autenticado para inicializar notificaciones');
      return
    }

    try {
      // Cargar notificaciones iniciales
      await cargarRecientes()
      await actualizarContador()

      // Conectar WebSocket
      conectarWebSocket(authStore.usuario.id)
    } catch (error) {
      console.error('❌ Error al inicializar notificaciones:', error)
    }
  }

  /**
   * Conectar WebSocket
   */
  function conectarWebSocket(profesionalId: number) {
    webSocketService.connect(profesionalId, (notificacion: NotificacionWebSocket) => {
      // Agregar notificación recibida al principio del array
      const nuevaNotificacion: Notificacion = {
        id: notificacion.id,
        tipo: notificacion.tipo,
        titulo: notificacion.titulo,
        mensaje: notificacion.mensaje,
        contenidoJson: notificacion.contenidoJson,
        turnoId: notificacion.turnoId,
        leida: false,
        fechaCreacion: notificacion.fechaCreacion
      }

      notificaciones.value.unshift(nuevaNotificacion)
      contadorNoLeidas.value++

      // Emitir evento personalizado para que otros componentes puedan reaccionar
      const event = new CustomEvent('nueva-notificacion', { 
        detail: nuevaNotificacion 
      })
      window.dispatchEvent(event)
    })

    conectado.value = true
  }

  /**
   * Desconectar WebSocket
   */
  function desconectar() {
    webSocketService.disconnect()
    conectado.value = false
  }

  /**
   * Cargar notificaciones recientes
   */
  async function cargarRecientes(limite: number = 20) {
    try {
      cargando.value = true
      const resultado = await notificacionesService.obtenerRecientes(limite)
      notificaciones.value = resultado
    } catch (error) {
      console.error('❌ Error al cargar notificaciones recientes:', error)
      notificaciones.value = [] // Asegurar que sea un array vacío en caso de error
    } finally {
      cargando.value = false
    }
  }

  /**
   * Cargar notificaciones no leídas
   */
  async function cargarNoLeidas() {
    try {
      cargando.value = true
      const noLeidas = await notificacionesService.obtenerNoLeidas()
      
      // Actualizar solo las no leídas en el array principal
      notificaciones.value = [
        ...noLeidas,
        ...notificaciones.value.filter(n => n.leida)
      ]
    } catch (error) {
      console.error('Error al cargar notificaciones no leídas:', error)
      throw error
    } finally {
      cargando.value = false
    }
  }

  /**
   * Actualizar contador de no leídas
   */
  async function actualizarContador() {
    try {
      contadorNoLeidas.value = await notificacionesService.contarNoLeidas()
    } catch (error) {
      console.error('Error al actualizar contador:', error)
    }
  }

  /**
   * Marcar notificación como leída
   */
  async function marcarComoLeida(id: number) {
    try {
      await notificacionesService.marcarComoLeida(id)
      
      // Actualizar en el estado local
      const notificacion = notificaciones.value.find(n => n.id === id)
      if (notificacion && !notificacion.leida) {
        notificacion.leida = true
        notificacion.fechaLectura = new Date().toISOString()
        contadorNoLeidas.value = Math.max(0, contadorNoLeidas.value - 1)
      }
    } catch (error) {
      console.error('Error al marcar notificación como leída:', error)
      throw error
    }
  }

  /**
   * Marcar todas como leídas
   */
  async function marcarTodasComoLeidas() {
    try {
      await notificacionesService.marcarTodasComoLeidas()
      
      // Actualizar en el estado local
      notificaciones.value.forEach(n => {
        if (!n.leida) {
          n.leida = true
          n.fechaLectura = new Date().toISOString()
        }
      })
      
      contadorNoLeidas.value = 0
    } catch (error) {
      console.error('Error al marcar todas como leídas:', error)
      throw error
    }
  }

  /**
   * Limpiar notificaciones
   */
  function limpiar() {
    notificaciones.value = []
    contadorNoLeidas.value = 0
    desconectar()
  }

  return {
    // Estado
    notificaciones,
    contadorNoLeidas,
    cargando,
    conectado,
    
    // Computed
    notificacionesNoLeidas,
    tieneNotificacionesNoLeidas,
    
    // Acciones
    inicializar,
    conectarWebSocket,
    desconectar,
    cargarRecientes,
    cargarNoLeidas,
    actualizarContador,
    marcarComoLeida,
    marcarTodasComoLeidas,
    limpiar
  }
})
