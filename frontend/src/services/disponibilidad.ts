import api from './api'

export type DiaSemana = 'LUNES' | 'MARTES' | 'MIERCOLES' | 'JUEVES' | 'VIERNES' | 'SABADO' | 'DOMINGO'

export interface DisponibilidadRequest {
  diaSemana: DiaSemana
  horaInicio: string // HH:mm
  horaFin: string // HH:mm
}

export interface DisponibilidadResponse {
  id: number | null
  diaSemana: DiaSemana
  horaInicio: string
  horaFin: string
  activo: boolean
}

export const disponibilidadService = {
  async obtenerHorariosEmpresa(): Promise<any[]> {
    const response = await api.get('/profesional/horarios-empresa')
    return response.data
  },

  async obtenerDisponibilidad(): Promise<DisponibilidadResponse[]> {
    const response = await api.get('/profesional/disponibilidad')
    return response.data
  },

  async inicializarDesdeEmpresa(): Promise<any> {
    const response = await api.post('/profesional/disponibilidad/inicializar-desde-empresa')
    return response.data
  },

  async crearDisponibilidad(datos: DisponibilidadRequest): Promise<DisponibilidadResponse> {
    const response = await api.post('/profesional/disponibilidad', datos)
    return response.data
  },

  async actualizarDisponibilidad(id: number, datos: DisponibilidadRequest): Promise<DisponibilidadResponse> {
    const response = await api.put(`/profesional/disponibilidad/${id}`, datos)
    return response.data
  },

  async eliminarDisponibilidad(id: number): Promise<void> {
    await api.delete(`/profesional/disponibilidad/${id}`)
  }
}

export const diasSemana: DiaSemana[] = [
  'LUNES',
  'MARTES',
  'MIERCOLES',
  'JUEVES',
  'VIERNES',
  'SABADO',
  'DOMINGO'
]
