<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useClienteStore } from '@/stores/cliente'
import api from '@/services/api'

const clienteStore = useClienteStore()

// Intentar recuperar sesión activa al cargar la aplicación
onMounted(async () => {
  if (!clienteStore.isAuthenticated) {
    try {
      const response = await api.obtenerPerfilCliente()
      if (response.data.exito) {
        clienteStore.setCliente(response.data.datos)
      }
    } catch {
      // Sin sesión activa, continuar normalmente
      // El store permanece con cliente=null
    }
  }
})
</script>
