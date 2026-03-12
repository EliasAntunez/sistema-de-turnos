import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

export interface Usuario {
  id?: number
  nombre: string
  apellido: string
  email: string
  // El backend devuelve un array de roles (e.g. ["DUENO", "PROFESIONAL"])
  roles: string[]
  empresaId?: number
  empresaNombre?: string
}

interface AuthCredentials {
  email: string
  contrasena: string
}

export const useAuthStore = defineStore('auth', () => {
  const usuario = ref<Usuario | null>(null)
  const autenticado = ref<boolean>(false)

  // ─── Helpers de rol ───────────────────────────────────────────────────────
  function hasRole(role: string): boolean {
    return usuario.value?.roles?.includes(role) ?? false
  }

  const isSuperAdmin = computed(() => hasRole('SUPER_ADMIN'))
  const isDueno      = computed(() => hasRole('DUENO'))
  const isProfesional = computed(() => hasRole('PROFESIONAL'))
  // ──────────────────────────────────────────────────────────────────────────

  function setUsuario(datosUsuario: Usuario) {
    usuario.value = datosUsuario
    autenticado.value = true
  }

  function logout() {
    usuario.value = null
    autenticado.value = false
  }

  async function cargarDesdeBackend(): Promise<boolean> {
    try {
      const response = await api.get('/auth/perfil')
      const payload = response.data?.datos ?? response.data

      if (response.data && response.data.exito === false) {
        return false
      }

      // Detectar sesión de cliente (no accede al panel de staff)
      const rolesPayload: string[] = payload?.roles ?? (payload?.rol ? [payload.rol] : [])
      if (rolesPayload.includes('CLIENTE')) {
        return false
      }

      if (payload) {
        // Normalizar: si el backend enviara el campo singular `rol` (legacy),
        // lo convertimos a array para mantener la interfaz uniforme.
        if (!payload.roles && payload.rol) {
          payload.roles = [payload.rol]
        }
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
    hasRole,
    isSuperAdmin,
    isDueno,
    isProfesional,
    setUsuario,
    logout,
    cargarDesdeBackend
  }
})
