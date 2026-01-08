import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'

interface AuthCredentials {
  email: string
  contrasena: string
}

const apiClient: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

// Interceptor para agregar autenticación
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const auth = localStorage.getItem('auth')
  if (auth) {
    const { email, contrasena } = JSON.parse(auth) as AuthCredentials
    config.auth = { username: email, password: contrasena }
  }
  return config
})

export default {
  // Autenticación
  login(credentials: AuthCredentials) {
    return apiClient.post('/auth/login', credentials)
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

  delete(url: string) {
    return apiClient.delete(url)
  }
}
