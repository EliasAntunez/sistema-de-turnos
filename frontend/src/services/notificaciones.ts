import api from './api'

export interface Notificacion {
  id: number
  tipo: string
  titulo: string
  mensaje: string
  contenidoJson?: string
  turnoId?: number
  leida: boolean
  fechaCreacion: string
  fechaLectura?: string
}

/**
 * Servicio para gestionar notificaciones vía API REST
 */
class NotificacionesService {
  
  /**
   * Obtener notificaciones no leídas
   */
  async obtenerNoLeidas(): Promise<Notificacion[]> {
    const response = await api.get('/profesional/notificaciones/no-leidas')
    return response.data.datos
  }

  /**
   * Contar notificaciones no leídas
   */
  async contarNoLeidas(): Promise<number> {
    const response = await api.get('/profesional/notificaciones/contador')
    return response.data.datos
  }

  /**
   * Obtener notificaciones recientes (últimas N)
   */
  async obtenerRecientes(limite: number = 20): Promise<Notificacion[]> {
    const response = await api.get(`/profesional/notificaciones/recientes?limite=${limite}`)
    return response.data.datos
  }

  /**
   * Obtener historial con paginación
   */
  async obtenerHistorial(pagina: number = 0, tamano: number = 20) {
    const response = await api.get(
      `/profesional/notificaciones?pagina=${pagina}&tamano=${tamano}`
    )
    return response.data
  }

  /**
   * Marcar notificación como leída
   */
  async marcarComoLeida(id: number): Promise<void> {
    await api.put(`/profesional/notificaciones/${id}/leer`)
  }

  /**
   * Marcar todas las notificaciones como leídas
   */
  async marcarTodasComoLeidas(): Promise<void> {
    await api.put('/profesional/notificaciones/leer-todas')
  }
}

export const notificacionesService = new NotificacionesService()
