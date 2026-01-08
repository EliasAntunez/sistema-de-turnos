import { defineStore } from 'pinia'
import { ref } from 'vue'

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

  function setUsuario(datosUsuario: Usuario, credenciales: AuthCredentials) {
    usuario.value = datosUsuario
    autenticado.value = true
    
    // Guardar credenciales para HTTP Basic Auth
    localStorage.setItem('auth', JSON.stringify(credenciales))
    localStorage.setItem('usuario', JSON.stringify(datosUsuario))
  }

  function logout() {
    usuario.value = null
    autenticado.value = false
    localStorage.removeItem('auth')
    localStorage.removeItem('usuario')
  }

  function cargarUsuario() {
    const usuarioGuardado = localStorage.getItem('usuario')
    const authGuardado = localStorage.getItem('auth')
    
    if (usuarioGuardado && authGuardado) {
      usuario.value = JSON.parse(usuarioGuardado) as Usuario
      autenticado.value = true
    }
  }

  return {
    usuario,
    autenticado,
    setUsuario,
    logout,
    cargarUsuario
  }
})
