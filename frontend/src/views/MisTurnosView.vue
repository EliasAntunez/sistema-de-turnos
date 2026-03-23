<template>
  <div class="min-h-screen bg-slate-50 bg-[radial-gradient(1200px_500px_at_50%_-20%,rgba(99,102,241,0.08),transparent)]">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div v-if="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
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
        <button
          type="button"
          @click="mostrarFiltrosMobile = !mostrarFiltrosMobile"
          class="md:hidden mb-4 inline-flex w-full items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold bg-white border border-slate-300 text-slate-700 transition-all duration-200 hover:scale-[1.02] hover:bg-slate-50 active:scale-95"
        >
          <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 3c2.755 0 5 2.245 5 5 0 1.305-.503 2.496-1.325 3.385-.52.562-.848 1.284-.848 2.05V15h-5.654v-1.565c0-.766-.328-1.488-.848-2.05A4.973 4.973 0 0 1 7 8c0-2.755 2.245-5 5-5Zm-2 14h4m-3 4h2" />
          </svg>
          {{ mostrarFiltrosMobile ? 'Ocultar Filtros' : 'Filtrar Turnos' }}
        </button>

        <div
          :class="[
            'bg-white/90 backdrop-blur-sm border border-slate-200/70 shadow-sm rounded-2xl p-4 gap-4 mb-6',
            mostrarFiltrosMobile ? 'grid grid-cols-1' : 'hidden',
            'md:grid md:grid-cols-3 lg:grid-cols-5'
          ]"
        >
          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-600">Periodo</label>
            <select
              v-model="filtros.periodo"
              @change="onCambioPeriodo"
              class="w-full rounded-xl border border-slate-300 bg-white/90 px-3 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-300"
            >
              <option value="todos">Todos</option>
              <option value="hoy">Hoy</option>
              <option value="semana">Esta semana</option>
              <option value="rango">Rango</option>
            </select>
          </div>

          <div v-if="filtros.periodo === 'rango'" class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-600">Desde</label>
            <input
              type="date"
              v-model="filtros.fechaDesde"
              @change="aplicarFiltros"
              class="w-full rounded-xl border border-slate-300 bg-white/90 px-3 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-300"
            />
          </div>

          <div v-if="filtros.periodo === 'rango'" class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-600">Hasta</label>
            <input
              type="date"
              v-model="filtros.fechaHasta"
              @change="aplicarFiltros"
              class="w-full rounded-xl border border-slate-300 bg-white/90 px-3 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-300"
            />
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-600">Estado</label>
            <select
              v-model="filtros.estado"
              @change="aplicarFiltros"
              class="w-full rounded-xl border border-slate-300 bg-white/90 px-3 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-300"
            >
              <option value="">Todos los estados</option>
              <option value="PENDIENTE_PAGO">Seña pendiente</option>
              <option value="PENDIENTE_CONFIRMACION">Pendiente de confirmación</option>
              <option value="CONFIRMADO">Confirmado</option>
              <option value="ATENDIDO">Atendido</option>
              <option value="NO_ASISTIO">No asistió</option>
              <option value="REPROGRAMADO">Reprogramado</option>
              <option value="CANCELADO">Cancelado</option>
            </select>
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs font-medium text-gray-600">Servicio</label>
            <select
              v-model="filtros.servicioId"
              @change="aplicarFiltros"
              class="w-full rounded-xl border border-slate-300 bg-white/90 px-3 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-300"
            >
              <option value="">Todos los servicios</option>
              <option v-for="servicio in serviciosFiltro" :key="servicio.id" :value="servicio.id">
                {{ servicio.nombre }}
              </option>
            </select>
          </div>
        </div>

        <div v-if="turnos.length === 0" class="text-center py-12">
          <p class="text-gray-500 text-lg">No tienes turnos registrados</p>
          <router-link :to="`/empresa/${empresaSlug}`" class="mt-4 inline-flex items-center justify-center rounded-xl px-5 py-3 text-sm font-semibold bg-slate-900 text-white transition-all duration-200 hover:scale-[1.02] hover:bg-slate-800 active:scale-95">Reservar un turno</router-link>
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="turno in turnos"
            :key="turno.id"
            class="bg-white/90 backdrop-blur-sm border border-slate-200/70 shadow-sm rounded-2xl p-4 sm:p-5 transition-all duration-300 hover:shadow-md"
          >
            <div class="flex flex-col gap-3">
              <div class="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                <div class="min-w-0">
                  <p class="text-sm font-semibold text-slate-900 truncate">{{ formatearFecha(turno.fecha) }}</p>
                  <p class="text-sm text-slate-600">{{ turno.horaInicio }} - {{ turno.horaFinServicio }}</p>
                </div>
                <span :class="getEstadoClass(turno.estado)" class="inline-flex w-fit items-center rounded-full px-2.5 py-1 text-xs font-medium shrink-0">{{ getEstadoTexto(turno.estado) }}</span>
              </div>

              <div class="space-y-1.5">
                <p class="text-sm text-slate-700 truncate"><span class="font-medium text-slate-900">Servicio:</span> {{ turno.servicioNombre }}</p>
                <p class="text-sm text-slate-700 truncate"><span class="font-medium text-slate-900">Profesional:</span> {{ turno.profesionalNombre }}</p>
              </div>

              <div v-if="turno.observaciones" class="rounded-xl border border-amber-200 bg-amber-50 p-3">
                <p class="text-sm text-slate-700"><span class="font-medium">Observaciones:</span> {{ turno.observaciones }}</p>
              </div>

              <div
                v-if="turno.estado === 'PENDIENTE_PAGO' || (!esEstadoFinalTurno(turno.estado) && !esTurnoPasado(turno))"
                class="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between"
              >
                <div>
                  <button
                    v-if="turno.estado === 'PENDIENTE_PAGO'"
                    type="button"
                    @click="toggleTurnoExpandido(turno.id)"
                    class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-50 text-amber-700 hover:bg-amber-100 rounded-lg text-sm font-semibold transition-colors"
                  >
                    Pagar seña
                    <svg
                      class="h-4 w-4 transition-transform duration-200"
                      :class="isTurnoExpandido(turno.id) ? 'rotate-180' : ''"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                      stroke-width="1.8"
                    >
                      <path stroke-linecap="round" stroke-linejoin="round" d="m19.5 8.25-7.5 7.5-7.5-7.5" />
                    </svg>
                  </button>
                </div>

                <div v-if="!esEstadoFinalTurno(turno.estado) && !esTurnoPasado(turno)" class="flex items-center gap-3">
                  <button
                    @click.prevent="openReprogramarModal(turno)"
                    :disabled="actionLoading"
                    class="text-sm font-medium text-slate-500 hover:text-slate-800 transition-colors"
                  >
                    Reprogramar
                  </button>
                  <button
                    @click.prevent="openCancelarModal(turno)"
                    :disabled="actionLoading"
                    class="text-sm font-medium text-rose-500 hover:text-rose-700 transition-colors"
                  >
                    Cancelar
                  </button>
                </div>
              </div>

              <div v-if="turno.estado === 'PENDIENTE_PAGO' && isTurnoExpandido(turno.id)" class="mt-3 rounded-xl border border-slate-200 bg-slate-50 p-4">
                <p class="flex items-center gap-2 text-sm font-semibold text-slate-900">
                  <svg class="h-5 w-5 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.6">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 8.25h19.5m-18 0A1.5 1.5 0 0 0 2.25 9.75v7.5a1.5 1.5 0 0 0 1.5 1.5h16.5a1.5 1.5 0 0 0 1.5-1.5v-7.5a1.5 1.5 0 0 0-1.5-1.5m-18 0V6.75A1.5 1.5 0 0 1 3.75 5.25h16.5a1.5 1.5 0 0 1 1.5 1.5v1.5" />
                  </svg>
                  Datos para tu seña
                </p>
                <div class="mt-3 bg-white border border-slate-200 rounded-lg p-3">
                  <p class="text-sm text-slate-700 font-mono whitespace-pre-line">{{ obtenerDatosBancariosTurno(turno) }}</p>
                </div>
                <button
                  v-if="obtenerTelefonoEmpresaTurno(turno)"
                  @click="abrirWhatsApp(turno)"
                  class="mt-4 inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold bg-[#25D366] text-white transition-all duration-200 hover:scale-[1.02] hover:bg-[#128C7E] active:scale-95"
                >
                  <svg class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
                    <path d="M20.52 3.48A11.79 11.79 0 0 0 12.06 0C5.53 0 .22 5.31.22 11.84c0 2.08.54 4.1 1.57 5.88L0 24l6.45-1.69a11.77 11.77 0 0 0 5.61 1.43h.01c6.53 0 11.84-5.31 11.84-11.84 0-3.16-1.23-6.12-3.39-8.42Zm-8.46 18.2h-.01a9.9 9.9 0 0 1-5.05-1.38l-.36-.22-3.83 1 1.02-3.73-.24-.38a9.89 9.89 0 0 1-1.51-5.23c0-5.45 4.43-9.88 9.88-9.88 2.64 0 5.12 1.03 6.99 2.9a9.81 9.81 0 0 1 2.89 6.98c0 5.45-4.43 9.88-9.88 9.88Zm5.42-7.42c-.3-.15-1.76-.87-2.03-.97-.27-.1-.47-.15-.67.15-.2.3-.77.97-.94 1.17-.17.2-.35.22-.65.07-.3-.15-1.25-.46-2.39-1.46-.88-.79-1.47-1.76-1.64-2.06-.17-.3-.02-.46.13-.61.14-.14.3-.35.45-.52.15-.17.2-.3.3-.5.1-.2.05-.37-.02-.52-.07-.15-.67-1.61-.92-2.2-.24-.58-.49-.5-.67-.5h-.57c-.2 0-.52.07-.79.37-.27.3-1.04 1.02-1.04 2.49 0 1.47 1.07 2.89 1.22 3.09.15.2 2.1 3.2 5.08 4.49.71.31 1.26.49 1.69.62.71.23 1.35.2 1.86.12.57-.09 1.76-.72 2.01-1.42.25-.7.25-1.3.17-1.42-.07-.12-.27-.2-.57-.35Z"/>
                  </svg>
                  Enviar Comprobante
                </button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="totalPages > 0" class="flex justify-center items-center gap-4 mt-8">
          <button
            @click="irPaginaAnterior"
            :disabled="currentPage === 0 || loading"
            class="inline-flex items-center justify-center rounded-xl px-4 py-2.5 text-sm font-semibold bg-white border border-slate-300 text-slate-700 transition-all duration-200 hover:scale-[1.02] hover:bg-slate-50 active:scale-95 disabled:bg-slate-100 disabled:text-slate-400 disabled:hover:scale-100 disabled:cursor-not-allowed"
          >
            Anterior
          </button>
          <span class="text-sm font-medium text-gray-700">Página {{ currentPage + 1 }} de {{ totalPages }}</span>
          <button
            @click="irPaginaSiguiente"
            :disabled="currentPage >= totalPages - 1 || loading"
            class="inline-flex items-center justify-center rounded-xl px-4 py-2.5 text-sm font-semibold bg-slate-900 text-white transition-all duration-200 hover:scale-[1.02] hover:bg-slate-800 active:scale-95 disabled:bg-slate-300 disabled:text-slate-100 disabled:hover:scale-100 disabled:cursor-not-allowed"
          >
            Siguiente
          </button>
        </div>
      </div>
    </div>

    <Toast />

    <!-- Modales: se renderizan dentro del mismo template -->
    

    <ConfirmModal
      :show="showCancelarModal"
      titulo="Confirmar cancelación de turno"
      :mensaje="`¿Confirmas la cancelación del turno ${selectedTurno?.fecha} a las ${selectedTurno?.horaInicio}?\nAl confirmar, se aplicará la política de cancelación vigente de la empresa (plazos, penalizaciones y/o bloqueos).`"
      textoConfirmar="Confirmar cancelación"
      textoCancelar="Volver"
      colorBoton="bg-red-600 hover:bg-red-700"
      @confirm="submitCancelar"
      @cancel="closeCancelarModal"
    >
      <template #body>
        <div>
          <label class="block text-sm font-medium text-gray-700">Motivo</label>
          <input v-model="motivoCancelForm" class="mt-1 block w-full border rounded p-2" placeholder="Opcional" />
        </div>
      </template>
    </ConfirmModal>

    <div v-if="showReprogramModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white/90 backdrop-blur-md border border-white/60 rounded-2xl p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto shadow-sm">
        <h3 class="text-lg font-semibold text-slate-900 mb-4">Reprogramar reserva</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Selecciona una nueva fecha</label>
            <input
              type="date"
              v-model="reprogramDate"
              :min="fechaMinima"
              class="mt-1 block w-full rounded-xl border border-slate-300 bg-white/90 px-3 py-2.5 text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-slate-300"
            />
          </div>
          
          <div v-if="reprogramDate">
            <label class="block text-sm font-medium text-slate-700 mb-2">Horarios disponibles</label>
            
            <div v-if="cargandoSlots" class="text-center py-4">
              <div class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-indigo-600"></div>
              <p class="mt-2 text-sm text-gray-600">Cargando horarios...</p>
            </div>
            
            <div v-else-if="slotsDisponibles.length === 0" class="text-center py-4 bg-slate-50 rounded-xl border border-slate-200">
              <p class="text-sm text-slate-600">No hay horarios disponibles para esta fecha.</p>
              <p class="text-xs text-slate-500 mt-1">Intenta con otra fecha.</p>
            </div>
            
            <div v-else class="grid grid-cols-2 sm:grid-cols-3 gap-2 max-h-60 overflow-y-auto">
              <button
                v-for="(slot, index) in slotsDisponibles"
                :key="index"
                @click="seleccionarSlotReprogram(slot)"
                :class="[
                  'px-3 py-2 rounded-xl text-sm font-medium border transition-all duration-200 hover:scale-[1.02] active:scale-95 focus:outline-none focus:ring-2 focus:ring-slate-300',
                  slotSeleccionado === slot
                    ? 'bg-slate-900 text-white border-slate-900'
                    : 'bg-white/90 text-slate-700 border-slate-300 hover:border-slate-500 hover:bg-slate-50'
                ]"
              >
                {{ formatearHoraSlot(slot.horaInicio) }}
              </button>
            </div>
          </div>
          
          <div v-if="slotSeleccionado" class="p-3 bg-indigo-50 rounded-xl border border-indigo-200">
            <p class="text-sm text-indigo-800">
              <strong>Nuevo horario seleccionado:</strong> {{ formatearHoraSlot(slotSeleccionado.horaInicio) }} - {{ calcularFinServicioSlot(slotSeleccionado.horaInicio) }}
            </p>
          </div>
        </div>
        
        <div class="mt-6 flex justify-end gap-2">
          <button @click="closeReprogramModal" class="inline-flex items-center justify-center rounded-xl px-4 py-2.5 text-sm font-semibold bg-white border border-slate-300 text-slate-700 transition-all duration-200 hover:scale-[1.02] hover:bg-slate-50 active:scale-95">
            Cancelar
          </button>
          <button 
            :disabled="!slotSeleccionado || actionLoading" 
            @click="submitReprogram" 
            :class="[
              'inline-flex items-center justify-center rounded-xl px-4 py-2.5 text-sm font-semibold transition-all duration-200',
              !slotSeleccionado || actionLoading
                ? 'bg-slate-300 text-slate-100 cursor-not-allowed'
                : 'bg-slate-900 text-white hover:scale-[1.02] hover:bg-slate-800 active:scale-95'
            ]"
          >
            {{ actionLoading ? 'Reprogramando...' : 'Reprogramar' }}
          </button>
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
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import publicoService, { type EmpresaPublica, type SlotDisponible } from '@/services/publico'
import { useClienteStore } from '@/stores/cliente'
import { useToastStore } from '@/composables/useToast'
import Toast from '@/components/Toast.vue'
import ConfirmModal from '@/components/ConfirmModal.vue'
import Footer from '@/components/Footer.vue'

interface Turno {
  id: number
  fecha: string
  horaInicio: string
  horaFin: string
  horaFinServicio: string
  duracionMinutos: number
  servicioId?: number
  profesionalId?: number
  servicioNombre: string
  profesionalNombre: string
  estado: string
  observaciones: string | null
  empresaTelefono?: string | null
  empresaDatosBancarios?: string | null
}

const route = useRoute()
const router = useRouter()
const clienteStore = useClienteStore()

const empresaSlug = ref(route.params.empresaSlug as string)
const empresa = ref<EmpresaPublica | null>(null)
const turnos = ref<Turno[]>([])
const serviciosFiltro = ref<Array<{ id: number; nombre: string }>>([])
const loading = ref(false)
const errorMessage = ref('')
const actionLoading = ref(false)
const mostrarFiltrosMobile = ref(false)
const showCancelarModal = ref(false)
const showReprogramModal = ref(false)
const reprogramDate = ref('')
const slotsDisponibles = ref<SlotDisponible[]>([])
const slotSeleccionado = ref<SlotDisponible | null>(null)
const cargandoSlots = ref(false)
const toast = useToastStore()
const selectedTurno = ref<Turno | null>(null)
// removed nombreClienteForm and observacionesForm: editing of nombre no longer allowed
const motivoCancelForm = ref('')
const filtros = ref<{
  periodo: 'todos' | 'hoy' | 'semana' | 'rango'
  estado: string
  servicioId: string
  fechaDesde: string | null
  fechaHasta: string | null
}>({
  periodo: 'todos',
  estado: '',
  servicioId: '',
  fechaDesde: null,
  fechaHasta: null
})
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = ref(10)
const OFFSET_ARGENTINA = '-03:00'
const turnosExpandidos = ref<number[]>([])

function isTurnoExpandido(turnoId: number): boolean {
  return turnosExpandidos.value.includes(turnoId)
}

function toggleTurnoExpandido(turnoId: number) {
  if (isTurnoExpandido(turnoId)) {
    turnosExpandidos.value = turnosExpandidos.value.filter(id => id !== turnoId)
    return
  }
  turnosExpandidos.value = [...turnosExpandidos.value, turnoId]
}

function obtenerTimestampInicioTurnoArgentina(turno: Turno): number {
  return new Date(`${turno.fecha}T${turno.horaInicio}:00${OFFSET_ARGENTINA}`).getTime()
}

function esTurnoPasado(turno: Turno): boolean {
  return obtenerTimestampInicioTurnoArgentina(turno) < Date.now()
}

// Obtener fecha mínima para reprogramación (hoy)
const fechaMinima = new Date().toISOString().split('T')[0]

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

  await cargarServiciosFiltro()

  cargarTurnos()
})

async function cargarServiciosFiltro() {
  try {
    const servicios = await publicoService.obtenerServicios(empresaSlug.value)
    serviciosFiltro.value = servicios.map(s => ({ id: s.id, nombre: s.nombre }))
  } catch (error) {
    console.error('Error al cargar servicios para filtro:', error)
    serviciosFiltro.value = []
  }
}

async function cargarTurnos(page = currentPage.value) {
  loading.value = true
  errorMessage.value = ''

  try {
    const params: Record<string, any> = {
      page,
      size: pageSize.value
    }

    if (filtros.value.estado) {
      params.estado = filtros.value.estado
    }
    if (filtros.value.servicioId) {
      params.servicioId = Number(filtros.value.servicioId)
    }
    if (filtros.value.fechaDesde) {
      params.fechaDesde = filtros.value.fechaDesde
    }
    if (filtros.value.fechaHasta) {
      params.fechaHasta = filtros.value.fechaHasta
    }
    const response = await api.obtenerMisTurnos(params)
    
    if (response.data.exito) {
      const pageData = response.data.datos
      const contenido = pageData.content || []
      turnos.value = filtrarReprogramadosSegunVista(contenido)
      totalPages.value = pageData.totalPages ?? 0
      currentPage.value = pageData.number ?? page
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

function filtrarReprogramadosSegunVista(turnosEntrada: Turno[]): Turno[] {
  const estadoSeleccionado = filtros.value.estado
  const esFiltroExplicitoReprogramado = estadoSeleccionado === 'REPROGRAMADO'
  const esVistaHistorial = filtros.value.periodo === 'todos'

  if (esFiltroExplicitoReprogramado || esVistaHistorial) {
    return turnosEntrada
  }

  return turnosEntrada.filter(turno => turno.estado !== 'REPROGRAMADO')
}

function aplicarFiltros() {
  cargarTurnos(0)
}

function toIsoDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function calcularRangoSemanaActual() {
  const hoy = new Date()
  const diaSemana = hoy.getDay()
  const diferenciaALunes = diaSemana === 0 ? -6 : 1 - diaSemana

  const inicioSemana = new Date(hoy)
  inicioSemana.setDate(hoy.getDate() + diferenciaALunes)

  const finSemana = new Date(inicioSemana)
  finSemana.setDate(inicioSemana.getDate() + 6)

  return {
    desde: toIsoDate(inicioSemana),
    hasta: toIsoDate(finSemana)
  }
}

function onCambioPeriodo() {
  if (filtros.value.periodo === 'todos') {
    filtros.value.fechaDesde = null
    filtros.value.fechaHasta = null
  } else if (filtros.value.periodo === 'hoy') {
    const hoy = toIsoDate(new Date())
    filtros.value.fechaDesde = hoy
    filtros.value.fechaHasta = hoy
  } else if (filtros.value.periodo === 'semana') {
    const rango = calcularRangoSemanaActual()
    filtros.value.fechaDesde = rango.desde
    filtros.value.fechaHasta = rango.hasta
  } else if (filtros.value.periodo === 'rango') {
    if (!filtros.value.fechaDesde && !filtros.value.fechaHasta) {
      const hoy = toIsoDate(new Date())
      filtros.value.fechaDesde = hoy
      filtros.value.fechaHasta = hoy
    }
  }

  aplicarFiltros()
}

function irPaginaAnterior() {
  if (currentPage.value > 0) {
    cargarTurnos(currentPage.value - 1)
  }
}

function irPaginaSiguiente() {
  if (currentPage.value < totalPages.value - 1) {
    cargarTurnos(currentPage.value + 1)
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
    'PENDIENTE_PAGO': 'Seña pendiente',
    'PENDIENTE_CONFIRMACION': 'Pendiente de confirmación',
    'RESERVADO': 'Reservado',
    'CONFIRMADO': 'Confirmado',
    'EN_ATENCION': 'En Atención',
    'ATENDIDO': 'Atendido',
    'REPROGRAMADO': 'Reprogramado',
    'CANCELADO': 'Cancelado'
  }
  return estados[estado] || estado
}

function getEstadoClass(estado: string): string {
  const clases: { [key: string]: string } = {
    'PENDIENTE_PAGO': 'bg-amber-100 text-amber-800',
    'PENDIENTE_CONFIRMACION': 'bg-orange-100 text-orange-800',
    'RESERVADO': 'bg-blue-100 text-blue-800',
    'CONFIRMADO': 'bg-green-100 text-green-800',
    'EN_ATENCION': 'bg-yellow-100 text-yellow-800',
    'ATENDIDO': 'bg-gray-100 text-gray-800',
    'REPROGRAMADO': 'bg-purple-100 text-purple-800',
    'CANCELADO': 'bg-red-100 text-red-800'
  }
  return clases[estado] || 'bg-gray-100 text-gray-800'
}

function esEstadoFinalTurno(estado: string): boolean {
  return ['CANCELADO', 'ATENDIDO', 'NO_ASISTIO', 'REPROGRAMADO'].includes(estado)
}

function obtenerDatosBancariosTurno(turno: Turno): string {
  const desdeTurno = turno.empresaDatosBancarios?.trim()
  const desdeEmpresa = empresa.value?.datosBancarios?.trim()
  return desdeTurno || desdeEmpresa || 'Aún no hay datos bancarios configurados. Contacta al local para coordinar el pago de la seña.'
}

function obtenerTelefonoEmpresaTurno(turno: Turno): string {
  const telefono = turno.empresaTelefono || empresa.value?.telefono || ''
  return telefono.replace(/\D/g, '')
}

function abrirWhatsApp(turno: Turno) {
  const telefono = obtenerTelefonoEmpresaTurno(turno)
  if (!telefono) {
    toast.showWarning('No hay teléfono de WhatsApp configurado para esta empresa')
    return
  }
  window.open(`https://wa.me/${telefono}`, '_blank')
}

async function onCancelar(turno: Turno) {
  openCancelarModal(turno)
}

function openReprogramarModal(turno: Turno) {
  if (esEstadoFinalTurno(turno.estado)) {
    toast.showWarning('No se puede reprogramar un turno en estado final')
    return
  }

  if (esTurnoPasado(turno)) {
    toast.showWarning('No se puede reprogramar un turno que ya pasó')
    return
  }

  selectedTurno.value = turno
  reprogramDate.value = turno.fecha
  slotsDisponibles.value = []
  slotSeleccionado.value = null
  
  showReprogramModal.value = true
  
  // Cargar slots para la fecha actual del turno
  cargarSlotsParaReprogramar()
}

function closeReprogramModal() {
  showReprogramModal.value = false
  selectedTurno.value = null
  slotsDisponibles.value = []
  slotSeleccionado.value = null
}

async function cargarSlotsParaReprogramar() {
  if (!selectedTurno.value || !reprogramDate.value) return
  
  const turno = selectedTurno.value
  if (!turno.servicioId || !turno.profesionalId) {
    toast.showWarning('Información del turno incompleta')
    return
  }
  
  try {
    cargandoSlots.value = true
    slotsDisponibles.value = await publicoService.obtenerDisponibilidad(
      empresaSlug.value,
      turno.servicioId,
      turno.profesionalId,
      reprogramDate.value,
      // Bloque total congelado del turno = duracion + buffer almacenados en horaFin - horaInicio
      calcularBloqueTotalMinutos(turno)
    )
    slotSeleccionado.value = null
  } catch (error: any) {
    console.error('Error al cargar slots:', error)
    toast.showError('Error al cargar horarios disponibles')
    slotsDisponibles.value = []
  } finally {
    cargandoSlots.value = false
  }
}

function seleccionarSlotReprogram(slot: SlotDisponible) {
  slotSeleccionado.value = slot
}

function formatearHoraSlot(isoDateTime: string): string {
  return isoDateTime.length >= 16 ? isoDateTime.substring(11, 16) : isoDateTime
}

/**
 * Calcula el bloque total en minutos de un turno (duracionMinutos + bufferMinutos).
 * Deriva el valor directamente de horaFin - horaInicio, que ya los incluye ambos.
 */
function calcularBloqueTotalMinutos(turno: Turno): number {
  const [hInicio, mInicio] = turno.horaInicio.split(':').map(Number) as [number, number]
  const [hFin, mFin] = turno.horaFin.split(':').map(Number) as [number, number]
  return (hFin * 60 + mFin) - (hInicio * 60 + mInicio)
}

/**
 * Hora fin visible al cliente en el modal de reprogramación:
 * horaInicio del slot + duracionMinutos del turno (sin buffer).
 */
function calcularFinServicioSlot(horaInicioISO: string): string {
  if (!selectedTurno.value) return ''
  if (horaInicioISO.length < 16) return ''

  const horaMinuto = horaInicioISO.substring(11, 16)
  const [horasStr, minutosStr] = horaMinuto.split(':')
  const horas = Number(horasStr)
  const minutos = Number(minutosStr)

  if (Number.isNaN(horas) || Number.isNaN(minutos)) return ''

  const minutosTotales = horas * 60 + minutos + selectedTurno.value.duracionMinutos
  const horasResultado = Math.floor(minutosTotales / 60) % 24
  const minutosResultado = minutosTotales % 60

  return `${String(horasResultado).padStart(2, '0')}:${String(minutosResultado).padStart(2, '0')}`
}

// Watch para recargar slots cuando cambia la fecha
const stopWatchReprogramDate = watch(reprogramDate, () => {
  if (showReprogramModal.value && selectedTurno.value) {
    cargarSlotsParaReprogramar()
  }
})

onBeforeUnmount(() => {
  stopWatchReprogramDate()
})

async function submitReprogram() {
  if (!selectedTurno.value || !slotSeleccionado.value) {
    toast.showWarning('Por favor selecciona un horario disponible')
    return
  }
  if (actionLoading.value) return
  actionLoading.value = true
  let reprogramacionExitosa = false
  try {
    try { await api.getCsrfToken() } catch (e) { /* ignorar */ }

    const slot = slotSeleccionado.value
    const horaInicio = formatearHoraSlot(slot.horaInicio)
    
    const payload: any = { 
      fecha: reprogramDate.value, 
      horaInicio: horaInicio,
      profesionalId: slot.profesionalId
    }

    const resp = await api.reprogramReservation(selectedTurno.value.id, payload)
    if (resp.status === 200) {
      reprogramacionExitosa = true
      await cargarTurnos()
      toast.showSuccess('Reserva reprogramada correctamente')
    } else {
      console.error('Reprogramar fallo', resp)
      toast.showError('No se pudo reprogramar: ' + (resp.data?.mensaje || resp.status))
    }
  } catch (e: any) {
    console.error(e)
    const status = e?.response?.status
    const backendMessage = e?.response?.data?.mensaje || e?.response?.data?.message
    if (status === 400 || status === 409) {
      toast.showError(backendMessage || 'No se pudo reprogramar por políticas del local')
    } else {
      toast.showError('Error al reprogramar: ' + (backendMessage || e.message || e))
    }
  } finally {
    actionLoading.value = false
    showReprogramModal.value = false
    if (reprogramacionExitosa) {
      closeReprogramModal()
    }
  }
}

function openCancelarModal(turno: Turno) {
  if (esEstadoFinalTurno(turno.estado)) {
    toast.showWarning('No se puede cancelar un turno en estado final')
    return
  }

  if (esTurnoPasado(turno)) {
    toast.showWarning('No se puede cancelar un turno que ya pasó')
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
      toast.showSuccess('Reserva cancelada correctamente')
    } else {
      console.error('Cancelar fallo', resp)
      toast.showError('No se pudo cancelar: ' + (resp.data?.mensaje || resp.status))
    }
  } catch (e: any) {
    console.error(e)
    closeCancelarModal()
    const status = e?.response?.status
    const backendMessage = e?.response?.data?.mensaje || e?.response?.data?.message
    if (status === 400 || status === 409) {
      toast.showError(backendMessage || 'No se pudo cancelar por políticas del local')
    } else {
      toast.showError('Error al cancelar: ' + (backendMessage || e.message || e))
    }
  } finally {
    actionLoading.value = false
  }
}
</script>

