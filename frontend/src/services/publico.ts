import axios, { type AxiosInstance } from 'axios'

// Cliente HTTP sin autenticación para endpoints públicos
const publicClient: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api/publico',
  headers: {
    'Content-Type': 'application/json'
  }
})

export interface EmpresaPublica {
  id: number
  nombre: string
  slug: string
  descripcion: string | null
  direccion: string | null
  ciudad: string | null
  provincia: string | null
  telefono: string | null
  email: string | null
  diasMaximosReserva: number
}

export interface ServicioPublico {
  id: number
  nombre: string
  descripcion: string | null
  duracionMinutos: number
  precio: number
}

export interface ProfesionalPublico {
  id: number
  nombre: string
  apellido: string
  descripcion: string | null
}

export interface SlotDisponible {
  horaInicio: string // ISO DateTime
  horaFin: string // ISO DateTime
  profesionalId: number
  profesionalNombre: string
}

export interface RegistroClienteRequest {
  nombre: string
  apellido: string
  email: string
  contrasena: string
  telefono: string
}

export interface CrearTurnoRequest {
  servicioId: number
  profesionalId: number
  fecha: string // YYYY-MM-DD
  horaInicio: string // HH:mm
  nombreCliente: string
  telefonoCliente: string
  emailCliente?: string
  observaciones?: string
}

export interface TurnoResponse {
  id: number
  servicioId: number
  servicioNombre: string
  profesionalId: number
  profesionalNombre: string
  profesionalApellido: string
  fecha: string
  horaInicio: string
  horaFin: string
  duracionMinutos: number
  precio: number
  estado: string
  clienteNombre: string
  clienteTelefono: string
  clienteEmail: string | null
  observaciones: string | null
}

export interface ApiResponse<T> {
  exito: boolean
  mensaje: string
  datos: T
}

export default {
  /**
   * Obtener información pública de una empresa por slug
   */
  async obtenerEmpresa(slug: string): Promise<EmpresaPublica> {
    const response = await publicClient.get<ApiResponse<EmpresaPublica>>(`/empresa/${slug}`)
    return response.data.datos
  },

  /**
   * Obtener servicios activos de una empresa
   */
  async obtenerServicios(slug: string): Promise<ServicioPublico[]> {
    const response = await publicClient.get<ApiResponse<ServicioPublico[]>>(`/empresa/${slug}/servicios`)
    return response.data.datos
  },

  /**
   * Obtener profesionales que pueden dar un servicio específico
   */
  async obtenerProfesionales(slug: string, servicioId: number): Promise<ProfesionalPublico[]> {
    const response = await publicClient.get<ApiResponse<ProfesionalPublico[]>>(
      `/empresa/${slug}/profesionales`,
      { params: { servicioId } }
    )
    return response.data.datos
  },

  /**
   * Obtener slots disponibles para reservar
   */
  async obtenerDisponibilidad(
    empresaSlug: string,
    servicioId: number,
    profesionalId: number,
    fecha: string // YYYY-MM-DD
  ): Promise<SlotDisponible[]> {
    const response = await publicClient.get<ApiResponse<SlotDisponible[]>>('/disponibilidad', {
      params: { empresaSlug, servicioId, profesionalId, fecha }
    })
    return response.data.datos
  },

  /**
   * Registrar un nuevo cliente
   */
  async registrarCliente(datos: RegistroClienteRequest): Promise<void> {
    await publicClient.post('/registro-cliente', datos)
  },

  /**
   * Crear un turno (reserva)
   */
  async crearTurno(empresaSlug: string, datos: CrearTurnoRequest): Promise<TurnoResponse> {
    const response = await publicClient.post<ApiResponse<TurnoResponse>>(
      `/empresa/${empresaSlug}/turnos`,
      datos
    )
    return response.data.datos
  },

  /**
   * Verificar si un teléfono tiene cuenta registrada (detección pasiva)
   */
  async verificarTelefonoRegistrado(empresaSlug: string, telefono: string): Promise<boolean> {
    try {
      const response = await publicClient.get<ApiResponse<boolean>>(
        `/empresa/${empresaSlug}/verificar-telefono`,
        { params: { telefono } }
      )
      return response.data.datos
    } catch {
      return false // En caso de error, asumir que no está registrado
    }
  }
}
