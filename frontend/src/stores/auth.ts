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

  function setUsuario(datosUsuario: Usuario) {
    usuario.value = datosUsuario
    autenticado.value = true
    
    // SOLO guardar datos del usuario (NO credenciales)
    // Las sesiones se gestionan con cookies HttpOnly del servidor
    localStorage.setItem('usuario', JSON.stringify(datosUsuario))
  }

  function logout() {
    usuario.value = null
    autenticado.value = false
    localStorage.removeItem('usuario')
  }

  function cargarUsuario() {
    const usuarioGuardado = localStorage.getItem('usuario')
    
    if (usuarioGuardado) {
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
