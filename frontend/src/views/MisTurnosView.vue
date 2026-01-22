<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <p class="mt-2 text-gray-600">Cargando turnos...</p>
      </div>

      <div v-else-if="errorMessage" class="rounded-md bg-red-50 p-4">
        <div class="flex">
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">{{ errorMessage }}</h3>
          </div>
        </div>
      </div>

      <div v-else>
        <div v-if="turnos.length === 0" class="text-center py-12">
          <p class="text-gray-500 text-lg">No tienes turnos registrados</p>
          <router-link :to="`/empresa/${empresaSlug}`" class="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700">Reservar un turno</router-link>
        </div>

        <div v-else class="space-y-4">
          <div v-for="turno in turnos" :key="turno.id" class="bg-white shadow rounded-lg p-6 hover:shadow-md transition-shadow">
            <div class="flex justify-between items-start">
              <div class="flex-1">
                <div class="flex items-center text-gray-900 mb-2">
                  <svg class="h-5 w-5 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <span class="font-semibold">{{ formatearFecha(turno.fecha) }}</span>
                  <span class="ml-2">{{ turno.horaInicio }} - {{ turno.horaFin }}</span>
                </div>

                <div class="flex items-center text-gray-700 mb-1">
                  <svg class="h-5 w-5 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                  </svg>
                  <span>{{ turno.servicioNombre }}</span>
                </div>

                <div class="flex items-center text-gray-700 mb-1">
                  <svg class="h-5 w-5 mr-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span>{{ turno.profesionalNombre }}</span>
                </div>

                <div v-if="turno.observaciones" class="mt-3 p-3 bg-yellow-50 rounded-md">
                  <p class="text-sm text-gray-700"><span class="font-medium">Observaciones:</span> {{ turno.observaciones }}</p>
                </div>
              </div>

              <div class="ml-4 flex flex-col items-end gap-3">
                <span :class="getEstadoClass(turno.estado)" class="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium">{{ getEstadoTexto(turno.estado) }}</span>
                <div class="flex gap-2 mt-2">
                    <button v-if="turno.estado !== 'CANCELADO'" @click.prevent="openReprogramarModal(turno)" :disabled="actionLoading" class="px-3 py-1 bg-indigo-100 text-indigo-800 rounded-md text-sm">Reprogramar</button>
                    <button v-if="turno.estado !== 'CANCELADO'" @click.prevent="openCancelarModal(turno)" :disabled="actionLoading" class="px-3 py-1 bg-red-100 text-red-800 rounded-md text-sm">Cancelar</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Toast @dismiss="toast.dismiss" />

    <!-- Modales: se renderizan dentro del mismo template -->
    

    <div v-if="showCancelarModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-md">
        <h3 class="text-lg font-semibold mb-4">Cancelar reserva</h3>
        <div class="space-y-3">
          <div>
            <label class="block text-sm font-medium text-gray-700">Motivo</label>
            <input v-model="motivoCancelForm" class="mt-1 block w-full border rounded p-2" placeholder="Opcional" />
          </div>
        </div>
        <div class="mt-4 flex justify-end gap-2">
          <button @click="closeCancelarModal" class="px-4 py-2 bg-gray-200 rounded">Volver</button>
          <button :disabled="actionLoading" @click="submitCancelar" class="px-4 py-2 bg-red-600 text-white rounded">Confirmar cancelación</button>
        </div>
      </div>
    </div>

    <div v-if="showReprogramModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-full max-w-md">
        <h3 class="text-lg font-semibold mb-4">Reprogramar reserva</h3>
        <div class="space-y-3">
          <div>
            <label class="block text-sm font-medium text-gray-700">Fecha</label>
            <input type="date" v-model="reprogramDate" class="mt-1 block w-full border rounded p-2" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Hora inicio</label>
            <input type="time" v-model="reprogramTime" class="mt-1 block w-full border rounded p-2" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Profesional (opcional)</label>
            <select v-model="selectedProfessionalId" class="mt-1 block w-full border rounded p-2">
              <option :value="null">Mantener profesional actual</option>
              <option v-for="p in professionals" :key="p.id" :value="p.id">{{ p.nombre }}</option>
            </select>
          </div>
        </div>
        <div class="mt-4 flex justify-end gap-2">
          <button @click="closeReprogramModal" class="px-4 py-2 bg-gray-200 rounded">Cancelar</button>
          <button :disabled="actionLoading" @click="submitReprogram" class="px-4 py-2 bg-indigo-600 text-white rounded">Reprogramar</button>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <Footer 
      v-if="empresa"
      :direccion="empresa.direccion"
      :ciudad="empresa.ciudad"
      :telefono="empresa.telefono"
      :email="empresa.email"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import publicoService, { type EmpresaPublica } from '@/services/publico'
import { useClienteStore } from '@/stores/cliente'
import { useToastStore } from '@/composables/useToast'
import Toast from '@/components/Toast.vue'
import Footer from '@/components/Footer.vue'

interface Turno {
  id: number
  fecha: string
  horaInicio: string
  horaFin: string
  servicioId?: number
  profesionalId?: number
  servicioNombre: string
  profesionalNombre: string
  estado: string
  observaciones: string | null
}

const route = useRoute()
const router = useRouter()
const clienteStore = useClienteStore()

const empresaSlug = ref(route.params.empresaSlug as string)
const empresa = ref<EmpresaPublica | null>(null)
const turnos = ref<Turno[]>([])
const loading = ref(false)
const errorMessage = ref('')
const actionLoading = ref(false)
const showCancelarModal = ref(false)
const showReprogramModal = ref(false)
const reprogramDate = ref('')
const reprogramTime = ref('')
const professionals = ref<{ id: number; nombre: string }[]>([])
const selectedProfessionalId = ref<number | null>(null)
const toast = useToastStore()
const selectedTurno = ref<Turno | null>(null)
// removed nombreClienteForm and observacionesForm: editing of nombre no longer allowed
const motivoCancelForm = ref('')

onMounted(async () => {
  // Verificar si el cliente está autenticado
  if (!clienteStore.isAuthenticated) {
    router.push(`/empresa/${empresaSlug.value}/login-cliente`)
    return
  }

  // Cargar datos de la empresa
  try {
    empresa.value = await publicoService.obtenerEmpresa(empresaSlug.value)
  } catch (error) {
    console.error('Error al cargar empresa:', error)
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

async function onCancelar(turno: Turno) {
  openCancelarModal(turno)
}

function openReprogramarModal(turno: Turno) {
  const inicio = new Date(`${turno.fecha}T${turno.horaInicio}:00`)
  const ahora = new Date()
  const diffMs = inicio.getTime() - ahora.getTime()

  if (inicio.getTime() < ahora.getTime()) {
    toast.show('No se puede reprogramar un turno que ya pasó')
    return
  }

  if (diffMs < 60 * 60 * 1000) {
    toast.show('No se puede reprogramar un turno con menos de 1 hora de anticipación')
    return
  }

  selectedTurno.value = turno
  reprogramDate.value = turno.fecha
  reprogramTime.value = turno.horaInicio
  selectedProfessionalId.value = turno.profesionalId ?? null
  // Cargar profesionales del servicio si es posible
  professionals.value = []
  if (turno.servicioId) {
    publicoService.obtenerProfesionales(empresaSlug.value, turno.servicioId)
      .then(list => {
        professionals.value = list.map(p => ({ id: p.id, nombre: p.nombre + ' ' + p.apellido }))
      })
      .catch(() => { /* ignorar errores de carga de profesionales */ })
  }

  showReprogramModal.value = true
}

function closeReprogramModal() {
  showReprogramModal.value = false
  selectedTurno.value = null
}

async function submitReprogram() {
  if (!selectedTurno.value) return
  if (actionLoading.value) return
  actionLoading.value = true
  try {
    try { await api.getCsrfToken() } catch (e) { /* ignorar */ }

    const payload: any = { fecha: reprogramDate.value, horaInicio: reprogramTime.value }
    if (selectedProfessionalId.value) payload.profesionalId = selectedProfessionalId.value

    const resp = await api.reprogramReservation(selectedTurno.value.id, payload)
    if (resp.status === 200) {
      await cargarTurnos()
      closeReprogramModal()
      toast.show('Reserva reprogramada correctamente')
    } else {
      console.error('Reprogramar fallo', resp)
      toast.show('No se pudo reprogramar: ' + (resp.data?.mensaje || resp.status))
    }
  } catch (e: any) {
    console.error(e)
    toast.show('Error al reprogramar: ' + (e.response?.data?.mensaje || e.message || e))
  } finally {
    actionLoading.value = false
  }
}

function openCancelarModal(turno: Turno) {
  const inicio = new Date(`${turno.fecha}T${turno.horaInicio}:00`)
  const ahora = new Date()
  const diffMs = inicio.getTime() - ahora.getTime()

  if (inicio.getTime() < ahora.getTime()) {
    toast.show('No se puede cancelar un turno que ya pasó')
    return
  }

  if (diffMs < 60 * 60 * 1000) {
    toast.show('No se puede cancelar un turno con menos de 1 hora de anticipación')
    return
  }

  selectedTurno.value = turno
  motivoCancelForm.value = ''
  showCancelarModal.value = true
}

function closeCancelarModal() {
  showCancelarModal.value = false
  selectedTurno.value = null
}

async function submitCancelar() {
  if (!selectedTurno.value) return
  if (actionLoading.value) return
  actionLoading.value = true
  try {
  // Asegurar CSRF cookie antes de la petición de cancelación
  try { await api.getCsrfToken() } catch (e) { /* ignorar */ }

  const resp = await api.cancelReservation(selectedTurno.value.id, { motivo: motivoCancelForm.value || 'Cancelado por cliente' })
    if (resp.status === 200) {
      await cargarTurnos()
      closeCancelarModal()
      toast.show('Reserva cancelada correctamente')
    } else {
      console.error('Cancelar fallo', resp)
      toast.show('No se pudo cancelar: ' + (resp.data?.mensaje || resp.status))
    }
  } catch (e: any) {
    console.error(e)
    toast.show('Error al cancelar: ' + (e.response?.data?.mensaje || e.message || e))
  } finally {
    actionLoading.value = false
  }
}
</script>

