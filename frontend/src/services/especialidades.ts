import api from './api'

export interface EspecialidadRequest {
  nombre: string
  descripcion?: string
}

export interface EspecialidadResponse {
  id: number
  nombre: string
  descripcion: string | null
  activa: boolean
  fechaCreacion: string
}

export const especialidadService = {
  async obtenerTodasLasEspecialidades(): Promise<EspecialidadResponse[]> {
    const response = await api.get('/admin/especialidades')
    return response.data.datos
  },

  async obtenerEspecialidadesActivas(): Promise<EspecialidadResponse[]> {
    const response = await api.get('/especialidades')
    return response.data.datos
  },

  async crearEspecialidad(datos: EspecialidadRequest): Promise<EspecialidadResponse> {
    const response = await api.post('/admin/especialidades', datos)
    return response.data.datos
  },

  async actualizarEspecialidad(id: number, datos: EspecialidadRequest): Promise<EspecialidadResponse> {
    const response = await api.put(`/admin/especialidades/${id}`, datos)
    return response.data.datos
  },

  async activarEspecialidad(id: number): Promise<void> {
    await api.patch(`/admin/especialidades/${id}/activar`)
  },

  async desactivarEspecialidad(id: number): Promise<void> {
    await api.patch(`/admin/especialidades/${id}/desactivar`)
  }
}
