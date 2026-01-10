import api from './api'

export interface BloqueoRequest {
  fechaInicio: string // YYYY-MM-DD
  fechaFin?: string | null // YYYY-MM-DD (opcional para rango)
  motivo?: string | null
}

export interface BloqueoResponse {
  id: number
  profesionalId: number
  fechaInicio: string
  fechaFin: string | null
  motivo: string | null
  activo: boolean
  fechaCreacion: string
}

export interface ConflictoTurno {
  turnoId: number
  fecha: string
  horaInicio: string
  horaFin: string
  clienteNombre: string
  clienteTelefono: string
  servicioNombre: string
  estado: string
}

export interface ConflictosBloqueoResponse {
  tieneConflictos: boolean
  cantidadConflictos: number
  turnosConflictivos: ConflictoTurno[]
  mensaje: string
}

export interface ReprogramacionTurno {
  turnoId: number
  nuevaFecha: string
  nuevaHora: string
}

export interface ResolucionConflictoRequest {
  bloqueo: BloqueoRequest
  accion: 'CANCELAR_TODOS' | 'REPROGRAMAR' | 'CANCELAR_FUTUROS'
  reprogramaciones?: ReprogramacionTurno[]
}

export interface SlotDisponible {
  fecha: string
  horaInicio: string
  horaFin: string
  dia: string
}

export const bloqueosService = {
  async obtenerBloqueos(): Promise<BloqueoResponse[]> {
    const response = await api.get('/profesional/bloqueos')
    return response.data
  },

  async verificarConflictos(datos: BloqueoRequest): Promise<ConflictosBloqueoResponse> {
    const response = await api.post('/profesional/bloqueos/verificar-conflictos', datos)
    return response.data.datos
  },

  async crearBloqueoConResolucion(datos: ResolucionConflictoRequest): Promise<BloqueoResponse> {
    const response = await api.post('/profesional/bloqueos/con-resolucion', datos)
    return response.data.datos
  },

  async sugerirSlots(turnoId: number, diasABuscar: number = 30): Promise<SlotDisponible[]> {
    const response = await api.get(`/profesional/bloqueos/slots-sugeridos/${turnoId}?diasABuscar=${diasABuscar}`)
    return response.data.datos
  },

  async crearBloqueo(datos: BloqueoRequest): Promise<BloqueoResponse> {
    const response = await api.post('/profesional/bloqueos', datos)
    return response.data
  },

  async actualizarBloqueo(id: number, datos: BloqueoRequest): Promise<BloqueoResponse> {
    const response = await api.put(`/profesional/bloqueos/${id}`, datos)
    return response.data
  },

  async eliminarBloqueo(id: number): Promise<void> {
    await api.delete(`/profesional/bloqueos/${id}`)
  }
}
