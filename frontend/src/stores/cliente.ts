import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface ClienteInfo {
  id: number
  nombre: string
  telefono: string
  email: string
  empresaId: number
  empresaNombre: string
}

export const useClienteStore = defineStore('cliente', () => {
  const cliente = ref<ClienteInfo | null>(null)
  const isAuthenticated = computed(() => cliente.value !== null)

  function setCliente(clienteData: ClienteInfo) {
    cliente.value = clienteData
  }

  function logout() {
    cliente.value = null
  }

  function cargarCliente() {
    // Ya no se utiliza localStorage para persistir sesión cliente.
    // El estado se reconstruye desde el backend llamando a `/api/auth/perfil`.
  }

  // Cargar cliente desde localStorage al inicializar el store
  // No cargar desde localStorage al iniciar

  return {
    cliente,
    isAuthenticated,
    setCliente,
    logout
  }
})
