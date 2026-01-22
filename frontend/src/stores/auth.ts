import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/services/api'

interface Usuario {
  id?: number
  nombre: string
  apellido: string
  email: string
  rol: string
}

interface AuthCredentials {
  email: string
  contrasena: string
}

export const useAuthStore = defineStore('auth', () => {
  const usuario = ref<Usuario | null>(null)
  const autenticado = ref<boolean>(false)

  function setUsuario(datosUsuario: Usuario) {
    usuario.value = datosUsuario
    autenticado.value = true
    // NO persistir en localStorage. El estado se reconstruye desde el backend.
  }

  function logout() {
    usuario.value = null
    autenticado.value = false
    // No persistimos en localStorage; solo limpiar el estado en memoria
  }

  async function cargarDesdeBackend(): Promise<boolean> {
    try {
      const response = await api.get('/auth/perfil')
      // Puede devolver RespuestaApi (usuarios) o ApiResponse (cliente)
      const payload = response.data?.datos ?? response.data

      // Si el backend indicó exito=false, no hay sesión
      if (response.data && response.data.exito === false) {
        return false
      }

      // Detectar si es cliente (tendrá campo empresaId o rol CLIENTE)
      // Nota: no seteamos el `cliente` globalmente desde aqui porque el cliente
      // es específico por empresa. Las vistas públicas deben consultar el perfil
      // y decidir si asociar la sesión al `cliente` según el `empresaSlug`/empresaId.
      if (payload && (payload.empresaId !== undefined || payload.rol === 'CLIENTE')) {
        return false
      }

      if (payload) {
        usuario.value = payload as Usuario
        autenticado.value = true
        return true
      }

      return false
    } catch (e) {
      return false
    }
  }

  return {
    usuario,
    autenticado,
    setUsuario,
    logout,
    cargarDesdeBackend
  }
})
