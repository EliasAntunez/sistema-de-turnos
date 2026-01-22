import api from './api'

export interface ServicioRequest {
  nombre: string
  descripcion: string
  duracionMinutos: number
  bufferMinutos?: number | null
  precio: number
  especialidades: string[]
}

export interface ServicioResponse {
  id: number
  nombre: string
  descripcion: string
  duracionMinutos: number
  bufferMinutos: number | null
  precio: number
  especialidades: string[]
  activo: boolean
  fechaCreacion: string
}

export const servicioService = {
  async obtenerServicios(): Promise<ServicioResponse[]> {
    const response = await api.get('/dueno/servicios')
    // El backend retorna directamente el array
    return Array.isArray(response.data) ? response.data : []
  },

  async crearServicio(datos: ServicioRequest): Promise<ServicioResponse> {
    const response = await api.post('/dueno/servicios', datos)
    return response.data
  },

  async actualizarServicio(id: number, datos: ServicioRequest): Promise<ServicioResponse> {
    const response = await api.put(`/dueno/servicios/${id}`, datos)
    return response.data
  },

  async activarServicio(id: number): Promise<void> {
    await api.patch(`/dueno/servicios/${id}/activar`)
  },

  async desactivarServicio(id: number): Promise<void> {
    await api.patch(`/dueno/servicios/${id}/desactivar`)
  }
}
