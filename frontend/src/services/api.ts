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
  
  if (!isExemptFromCsrf && ['post', 'put', 'patch', 'delete', 'get'].includes(config.method?.toLowerCase() || '')) {
    const csrfToken = getCsrfToken()
    if (csrfToken && config.headers) {
      config.headers['X-XSRF-TOKEN'] = csrfToken
    }
  }
  return config
})

// Interceptor para manejar sesión expirada o inválida
let authCleanupDone = false // evita limpiar/redirigir múltiples veces en la misma pestaña

apiClient.interceptors.response.use(
  response => response,
  async error => {
    const status = error.response?.status
    const isUnauthorized = status === 401 || status === 403

    // Si el caller indicó que NO quiere el redirect automático, respetarlo
    const skipRedirect = error.config?.headers?.['X-Skip-Auth-Redirect'] === 'true'

    if (isUnauthorized) {
      // Import dinámico de stores para limpiar estado (evita ciclos de import)
      try {
        const [authMod, clienteMod] = await Promise.allSettled([
          import('@/stores/auth'),
          import('@/stores/cliente')
        ])

        if (authMod.status === 'fulfilled') {
          try { authMod.value.useAuthStore().logout() } catch (e) {}
        }
        if (clienteMod.status === 'fulfilled') {
          try { clienteMod.value.useClienteStore().logout() } catch (e) {}
        }
      } catch (e) {
        try { localStorage.removeItem('usuario') } catch (e) {}
        try { localStorage.removeItem('cliente') } catch (e) {}
      }

      // Si el caller solicitó no redirect, devolver la respuesta de error como resuelta
      if (skipRedirect) {
        return Promise.resolve(error.response)
      }

      // Evitar múltiples limpiezas seguidas en la misma pestaña
      if (!authCleanupDone) {
        authCleanupDone = true
        // Ya limpiamos `auth` y `cliente` arriba; no forzamos navegación global.
        // Las vistas pueden decidir redirigir según contexto (por ejemplo, login-cliente con slug).
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

  // Modificar reserva como cliente autenticado
  modifyReservation(turnoId: number, datos: { nombreCliente?: string; observaciones?: string }) {
    return apiClient.put(`/cliente/reservas/${turnoId}`, datos)
  },

  // Cancelar reserva como cliente autenticado
  cancelReservation(turnoId: number, datos: { motivo: string }) {
    return apiClient.post(`/cliente/reservas/${turnoId}/cancelar`, datos)
  },

  // Reprogramar reserva como cliente autenticado
  reprogramReservation(turnoId: number, datos: { fecha: string; horaInicio: string; profesionalId?: number }) {
    return apiClient.put(`/cliente/reservas/${turnoId}/reprogramar`, datos)
  },

  obtenerPerfilCliente() {
    // Esta llamada puede ejecutarse desde páginas públicas para detectar sesión de cliente.
    // Evitar que el interceptor haga redirect automático en ese caso.
    // Usar el endpoint unificado de auth que devuelve exito=false cuando no hay sesión
    // y evita que Spring devuelva 403 por autorización.
    return apiClient.get('/auth/perfil', {
      headers: {
        'X-Skip-Auth-Redirect': 'true'
      }
    })
  }
}
