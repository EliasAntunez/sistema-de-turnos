import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'

interface AuthCredentials {
  email: string
  contrasena: string
}

const apiClient: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  },
  // CRÍTICO: Habilitar envío de cookies de sesión
  withCredentials: true
})

// Función para obtener el CSRF token de las cookies
function getCsrfToken(): string | null {
  const cookies = document.cookie.split('; ')
  const csrfCookie = cookies.find(row => row.startsWith('XSRF-TOKEN='))
  return csrfCookie ? (csrfCookie.split('=')[1] || null) : null
}

// Interceptor para agregar CSRF token a requests que modifican datos
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  // Excluir endpoints que no requieren CSRF según backend
  const isExemptFromCsrf = 
    config.url?.startsWith('/auth/login') || 
    config.url?.startsWith('/auth/logout') ||
    config.url?.startsWith('/publico/') // Endpoints públicos no requieren CSRF
  
  if (!isExemptFromCsrf && ['post', 'put', 'patch', 'delete'].includes(config.method?.toLowerCase() || '')) {
    const csrfToken = getCsrfToken()
    if (csrfToken && config.headers) {
      config.headers['X-XSRF-TOKEN'] = csrfToken
    }
  }
  return config
})

// Interceptor para manejar sesión expirada
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Solo redirigir si NO estamos en páginas de autenticación
      const currentPath = window.location.pathname
      const isAuthPage = currentPath === '/login' || 
                        currentPath.includes('/login-cliente') || 
                        currentPath.includes('/registro-cliente')
      
      if (!isAuthPage) {
        // Sesión expirada - limpiar y redirigir
        localStorage.removeItem('usuario')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default {
  // Autenticación
  login(credentials: AuthCredentials) {
    // Spring Security formLogin requiere x-www-form-urlencoded
    const formData = new URLSearchParams()
    formData.append('username', credentials.email)
    formData.append('password', credentials.contrasena)
    
    return apiClient.post('/auth/login', formData, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
    })
  },

  // Cerrar sesión
  logout() {
    return apiClient.post('/auth/logout')
  },

  // Obtener CSRF token (genera la cookie XSRF-TOKEN si no existe)
  getCsrfToken() {
    return apiClient.get('/auth/csrf')
  },

  registrarSuperAdmin(datos: any) {
    return apiClient.post('/auth/registrar-super-admin', datos)
  },

  // Empresas (SuperAdmin)
  crearEmpresaConDueno(datos: any) {
    return apiClient.post('/admin/empresas', datos)
  },

  obtenerEmpresas() {
    return apiClient.get('/admin/empresas')
  },

  obtenerEmpresa(id: number) {
    return apiClient.get(`/admin/empresas/${id}`)
  },

  activarEmpresa(id: number) {
    return apiClient.put(`/admin/empresas/${id}/activar`)
  },

  desactivarEmpresa(id: number) {
    return apiClient.put(`/admin/empresas/${id}/desactivar`)
  },

  // Horarios de Empresa (Dueño)
  crearHorarioEmpresa(datos: any) {
    return apiClient.post('/dueno/horarios-empresa', datos)
  },

  obtenerHorariosEmpresa() {
    return apiClient.get('/dueno/horarios-empresa')
  },

  actualizarHorarioEmpresa(id: number, datos: any) {
    return apiClient.put(`/dueno/horarios-empresa/${id}`, datos)
  },

  eliminarHorarioEmpresa(id: number) {
    return apiClient.delete(`/dueno/horarios-empresa/${id}`)
  },

  copiarHorariosAOtrosDias(diaFuente: string, diasDestino: string[], reemplazar: boolean) {
    return apiClient.post('/dueno/horarios-empresa/copiar', {
      diaFuente,
      diasDestino,
      reemplazar
    })
  },

  // Métodos genéricos para usar con cualquier endpoint
  get(url: string) {
    return apiClient.get(url)
  },

  post(url: string, data?: any) {
    return apiClient.post(url, data)
  },

  put(url: string, data?: any) {
    return apiClient.put(url, data)
  },

  patch(url: string, data?: any) {
    return apiClient.patch(url, data)
  },

  delete(url: string) {
    return apiClient.delete(url)
  },

  // Turnos (Profesional)
  obtenerTurnosProfesional(params?: { fecha?: string; fechaDesde?: string; fechaHasta?: string }) {
    return apiClient.get('/profesional/turnos', { params })
  },

  cambiarEstadoTurno(turnoId: number, datos: { nuevoEstado: string; observaciones?: string }) {
    return apiClient.put(`/profesional/turnos/${turnoId}/estado`, datos)
  },

  agregarObservacionesTurno(turnoId: number, datos: { observaciones: string }) {
    return apiClient.put(`/profesional/turnos/${turnoId}/observaciones`, datos)
  },

  // Cliente - Autenticación
  registrarCliente(empresaSlug: string, datos: { telefono: string; email: string; contrasena: string; confirmarContrasena: string }) {
    return apiClient.post(`/publico/empresa/${empresaSlug}/registro-cliente`, datos)
  },

  loginCliente(empresaSlug: string, credenciales: { telefono: string; contrasena: string }) {
    return apiClient.post(`/publico/empresa/${empresaSlug}/login-cliente`, credenciales)
  },

  // Cliente - Endpoints protegidos
  obtenerMisTurnos() {
    return apiClient.get('/cliente/mis-turnos')
  },

  obtenerPerfilCliente() {
    return apiClient.get('/cliente/perfil')
  }
}
