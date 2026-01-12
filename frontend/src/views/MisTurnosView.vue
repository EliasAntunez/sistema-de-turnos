<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header -->
    <div class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center py-6">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">Mis Turnos</h1>
            <p class="mt-1 text-sm text-gray-500" v-if="clienteStore.cliente">
              {{ clienteStore.cliente.nombre }} - {{ clienteStore.cliente.empresaNombre }}
            </p>
          </div>
          <button 
            @click="cerrarSesion"
            class="inline-flex items-center px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Cerrar Sesión
          </button>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Loading -->
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <p class="mt-2 text-gray-600">Cargando turnos...</p>
      </div>

      <!-- Error -->
      <div v-else-if="errorMessage" class="rounded-md bg-red-50 p-4">
        <div class="flex">
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">
              {{ errorMessage }}
            </h3>
          </div>
        </div>
      </div>

      <!-- Turnos -->
      <div v-else>
        <!-- Sin turnos -->
        <div v-if="turnos.length === 0" class="text-center py-12">
          <p class="text-gray-500 text-lg">No tienes turnos registrados</p>
          <router-link 
            :to="`/empresa/${empresaSlug}`"
            class="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            Reservar un turno
          </router-link>
        </div>

        <!-- Lista de turnos -->
        <div v-else class="space-y-4">
          <div 
            v-for="turno in turnos" 
            :key="turno.id"
            class="bg-white shadow rounded-lg p-6 hover:shadow-md transition-shadow"
          >
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <!-- Fecha y Hora -->
                <div class="flex items-center text-gray-900 mb-2">
                  <svg class="h-5 w-5 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <span class="font-semibold">{{ formatearFecha(turno.fecha) }}</span>
                  <span class="ml-2">{{ turno.horaInicio }} - {{ turno.horaFin }}</span>
                </div>

                <!-- Servicio -->
                <div class="flex items-center text-gray-700 mb-1">
                  <svg class="h-5 w-5 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                  </svg>
                  <span>{{ turno.servicioNombre }}</span>
                </div>

                <!-- Profesional -->
                <div class="flex items-center text-gray-700 mb-1">
                  <svg class="h-5 w-5 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span>{{ turno.profesionalNombre }}</span>
                </div>

                <!-- Observaciones (si existen) -->
                <div v-if="turno.observaciones" class="mt-3 p-3 bg-yellow-50 rounded-md">
                  <p class="text-sm text-gray-700">
                    <span class="font-medium">Observaciones:</span> {{ turno.observaciones }}
                  </p>
                </div>
              </div>

              <!-- Estado -->
              <div class="ml-4">
                <span 
                  :class="getEstadoClass(turno.estado)"
                  class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium"
                >
                  {{ getEstadoTexto(turno.estado) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import { useClienteStore } from '@/stores/cliente'

interface Turno {
  id: number
  fecha: string
  horaInicio: string
  horaFin: string
  servicioNombre: string
  profesionalNombre: string
  estado: string
  observaciones: string | null
}

const route = useRoute()
const router = useRouter()
const clienteStore = useClienteStore()

const empresaSlug = ref(route.params.empresaSlug as string)
const turnos = ref<Turno[]>([])
const loading = ref(false)
const errorMessage = ref('')

onMounted(() => {
  // Verificar si el cliente está autenticado
  if (!clienteStore.isAuthenticated) {
    router.push(`/empresa/${empresaSlug.value}/login-cliente`)
    return
  }
  
  cargarTurnos()
})

async function cargarTurnos() {
  loading.value = true
  errorMessage.value = ''

  try {
    const response = await api.obtenerMisTurnos()
    
    if (response.data.exito) {
      turnos.value = response.data.datos
    } else {
      errorMessage.value = response.data.mensaje || 'Error al cargar turnos'
    }
  } catch (error: any) {
    console.error('Error al cargar turnos:', error)
    
    if (error.response?.status === 401) {
      // Sesión expirada
      clienteStore.logout()
      router.push(`/empresa/${empresaSlug.value}/login-cliente`)
    } else {
      errorMessage.value = 'Error al cargar los turnos. Intenta nuevamente.'
    }
  } finally {
    loading.value = false
  }
}

function cerrarSesion() {
  clienteStore.logout()
  router.push(`/empresa/${empresaSlug.value}`)
}

function formatearFecha(fecha: string): string {
  const date = new Date(fecha + 'T00:00:00')
  return date.toLocaleDateString('es-AR', { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
}

function getEstadoTexto(estado: string): string {
  const estados: { [key: string]: string } = {
    'RESERVADO': 'Reservado',
    'CONFIRMADO': 'Confirmado',
    'EN_ATENCION': 'En Atención',
    'ATENDIDO': 'Atendido',
    'CANCELADO': 'Cancelado'
  }
  return estados[estado] || estado
}

function getEstadoClass(estado: string): string {
  const clases: { [key: string]: string } = {
    'RESERVADO': 'bg-blue-100 text-blue-800',
    'CONFIRMADO': 'bg-green-100 text-green-800',
    'EN_ATENCION': 'bg-yellow-100 text-yellow-800',
    'ATENDIDO': 'bg-gray-100 text-gray-800',
    'CANCELADO': 'bg-red-100 text-red-800'
  }
  return clases[estado] || 'bg-gray-100 text-gray-800'
}
</script>
