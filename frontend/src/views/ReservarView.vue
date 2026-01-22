<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header removed: company header moved to unified navbar to avoid duplication -->

    <!-- Contenido principal con padding -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">

    <!-- Indicador de pasos (Stepper responsive) -->
    <div class="relative mb-8">
      <!-- Línea de progreso -->
      <div class="absolute top-5 left-0 right-0 h-0.5 bg-gray-200 hidden sm:block" style="left: 10%; right: 10%;"></div>
      
      <!-- Pasos -->
      <div class="flex justify-between items-start relative">
        <!-- Paso 1 -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm transition-all',
            pasoActual === 1 ? 'bg-blue-600 text-white ring-4 ring-blue-100' : 
            pasoActual > 1 ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-600'
          ]">
            <span v-if="pasoActual > 1">✓</span>
            <span v-else>1</span>
          </div>
          <span class="text-xs sm:text-sm font-medium text-gray-700 text-center hidden sm:block">
            Servicio
          </span>
        </div>

        <!-- Paso 2 -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm transition-all',
            pasoActual === 2 ? 'bg-blue-600 text-white ring-4 ring-blue-100' : 
            pasoActual > 2 ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-600'
          ]">
            <span v-if="pasoActual > 2">✓</span>
            <span v-else>2</span>
          </div>
          <span class="text-xs sm:text-sm font-medium text-gray-700 text-center hidden sm:block">
            Profesional
          </span>
        </div>

        <!-- Paso 3 -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm transition-all',
            pasoActual === 3 ? 'bg-blue-600 text-white ring-4 ring-blue-100' : 
            pasoActual > 3 ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-600'
          ]">
            <span v-if="pasoActual > 3">✓</span>
            <span v-else>3</span>
          </div>
          <span class="text-xs sm:text-sm font-medium text-gray-700 text-center hidden sm:block">
            Fecha
          </span>
        </div>

        <!-- Paso 4 -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm transition-all',
            pasoActual === 4 ? 'bg-blue-600 text-white ring-4 ring-blue-100' : 
            pasoActual > 4 ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-600'
          ]">
            <span v-if="pasoActual > 4">✓</span>
            <span v-else>4</span>
          </div>
          <span class="text-xs sm:text-sm font-medium text-gray-700 text-center hidden sm:block">
            Hora
          </span>
        </div>

        <!-- Paso 5 -->
        <div class="flex flex-col items-center gap-2 flex-1">
          <div :class="[
            'w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm transition-all',
            pasoActual === 5 ? 'bg-blue-600 text-white ring-4 ring-blue-100' : 'bg-gray-200 text-gray-600'
          ]">
            5
          </div>
          <span class="text-xs sm:text-sm font-medium text-gray-700 text-center hidden sm:block">
            Confirmar
          </span>
        </div>
      </div>
    </div>

    <!-- Paso 1: Seleccionar Servicio -->
    <div v-if="pasoActual === 1" class="paso-content">
      <h2>Selecciona un servicio</h2>
      <div v-if="cargandoServicios" class="loading">Cargando servicios...</div>
      <div v-else-if="servicios.length === 0" class="empty-state">
        No hay servicios disponibles
      </div>
      <div v-else class="servicios-grid">
        <div
          v-for="servicio in servicios"
          :key="servicio.id"
          class="servicio-card"
          @click="seleccionarServicio(servicio)"
        >
          <h3>{{ servicio.nombre }}</h3>
          <p v-if="servicio.descripcion" class="descripcion">{{ servicio.descripcion }}</p>
          <div class="servicio-details">
            <span class="duracion">⏱️ {{ servicio.duracionMinutos }} min</span>
            <span class="precio">${{ servicio.precio }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Paso 2: Seleccionar Profesional -->
    <div v-if="pasoActual === 2" class="paso-content">
      <h2>Selecciona un profesional</h2>
      <button class="btn-secondary btn-back" @click="volverPaso(1)">← Volver</button>
      
      <div v-if="cargandoProfesionales" class="loading">Cargando profesionales...</div>
      <div v-else-if="profesionales.length === 0" class="empty-state">
        No hay profesionales disponibles para este servicio
      </div>
      <div v-else class="profesionales-list">
        <div
          v-for="profesional in profesionales"
          :key="profesional.id"
          class="profesional-card"
          @click="seleccionarProfesional(profesional)"
        >
          <div class="profesional-info">
            <h3>{{ profesional.nombre }} {{ profesional.apellido }}</h3>
            <p v-if="profesional.descripcion">{{ profesional.descripcion }}</p>
          </div>
          <span class="arrow">→</span>
        </div>
      </div>
    </div>

    <!-- Paso 3: Seleccionar Fecha -->
    <div v-if="pasoActual === 3" class="paso-content">
      <h2>Selecciona una fecha</h2>
      <button class="btn-secondary btn-back" @click="volverPaso(2)">← Volver</button>
      
      <div class="calendario-container">
        <div class="calendario-header">
          <button @click="mesAnterior" :disabled="!puedeMesAnterior">←</button>
          <h3>{{ mesActualNombre }} {{ añoActual }}</h3>
          <button @click="mesSiguiente" :disabled="!puedeMesSiguiente">→</button>
        </div>
        
        <div class="calendario-grid">
          <div class="dia-header" v-for="dia in diasSemana" :key="dia">{{ dia }}</div>
          <div
            v-for="(dia, index) in diasMes"
            :key="index"
            :class="['dia-cell', {
              'disabled': dia.disabled,
              'selected': dia.fecha === fechaSeleccionada,
              'hoy': dia.esHoy
            }]"
            @click="!dia.disabled && seleccionarFecha(dia.fecha)"
          >
            <span v-if="dia.numero">{{ dia.numero }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Paso 4: Seleccionar Hora -->
    <div v-if="pasoActual === 4" class="paso-content">
      <h2>Selecciona un horario</h2>
      <button class="btn-secondary btn-back" @click="volverPaso(3)">← Volver</button>
      
      <p class="fecha-seleccionada">📅 {{ fechaSeleccionadaFormateada }}</p>
      
      <div v-if="cargandoSlots" class="loading">Cargando horarios disponibles...</div>
      <div v-else-if="slots.length === 0" class="empty-state">
        No hay horarios disponibles para esta fecha
      </div>
      <div v-else class="slots-list">
        <button
          v-for="slot in slots"
          :key="slot.horaInicio"
          class="slot-btn"
          @click="seleccionarSlot(slot)"
        >
          {{ formatearHora(slot.horaInicio) }}
        </button>
      </div>
    </div>

    <!-- Paso 5: Confirmación -->
    <div v-if="pasoActual === 5" class="paso-content">
      <h2>Confirma tu reserva</h2>
      <button class="btn-secondary btn-back" @click="volverPaso(4)">← Volver</button>
      
      <div class="resumen-reserva">
        <div class="resumen-item">
          <strong>Servicio:</strong>
          <span>{{ servicioSeleccionado?.nombre }}</span>
        </div>
        <div class="resumen-item">
          <strong>Duración:</strong>
          <span>{{ servicioSeleccionado?.duracionMinutos }} minutos</span>
        </div>
        <div class="resumen-item">
          <strong>Precio:</strong>
          <span>${{ servicioSeleccionado?.precio }}</span>
        </div>
        <div class="resumen-item">
          <strong>Profesional:</strong>
          <span>{{ profesionalSeleccionado?.nombre }} {{ profesionalSeleccionado?.apellido }}</span>
        </div>
        <div class="resumen-item">
          <strong>Fecha:</strong>
          <span>{{ fechaSeleccionadaFormateada }}</span>
        </div>
        <div class="resumen-item">
          <strong>Hora:</strong>
          <span>{{ slotSeleccionado ? formatearHora(slotSeleccionado.horaInicio) : '' }}</span>
        </div>
      </div>

      <!-- Cliente autenticado: datos pre-llenados -->
      <div v-if="clienteAutenticado" class="cliente-autenticado-box">
        <div class="bg-gradient-to-r from-green-50 to-emerald-50 border-2 border-green-200 rounded-lg p-6 mb-6">
          <div class="flex items-start gap-4">
            <div class="flex-shrink-0">
              <div class="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
                <svg class="w-7 h-7 text-white" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
                </svg>
              </div>
            </div>
            <div class="flex-1">
              <h3 class="text-lg font-bold text-green-900 mb-2">Reservando como usuario registrado</h3>
              <div class="space-y-1 text-sm text-green-800">
                <p><strong>Nombre:</strong> {{ clienteStore.cliente?.nombre }}</p>
                <p><strong>Teléfono:</strong> {{ clienteStore.cliente?.telefono }}</p>
                <p v-if="clienteStore.cliente?.email"><strong>Email:</strong> {{ clienteStore.cliente.email }}</p>
              </div>
              <div class="mt-3 flex items-center gap-2 text-xs text-green-700">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
                </svg>
                <span>Este turno se agregará automáticamente a tu historial</span>
              </div>
            </div>
          </div>
        </div>
        <button @click="confirmarReserva" class="btn-primary btn-confirmar" :disabled="cargando">
          <span v-if="!cargando">✓ Confirmar Turno</span>
          <span v-else>Procesando...</span>
        </button>
      </div>

      <!-- Cliente invitado: formulario completo -->
      <div v-else class="formulario-cliente">
        <h3>Tus datos</h3>
        <form @submit.prevent="confirmarReserva">
          <div class="form-group">
            <label>Nombre *</label>
            <input v-model="datosCliente.nombre" type="text" required placeholder="Ingresa tu nombre completo" />
          </div>
          <div class="form-group">
            <label>Teléfono *</label>
            <input v-model="datosCliente.telefono" type="tel" required placeholder="Ej: +54 9 11 1234-5678" />
          </div>
          <div class="form-group">
            <label>Email (opcional)</label>
            <input v-model="datosCliente.email" type="email" placeholder="tu@email.com" />
          </div>
          
          <div class="form-group checkbox-group">
            <label class="checkbox-label">
              <input v-model="datosCliente.aceptaCondiciones" type="checkbox" required />
              <span>
                Acepto las
                <span 
                  class="politicas-link-azul"
                  @click="abrirModalPoliticas"
                  style="color: #1976d2; text-decoration: underline; cursor: pointer; font-weight: 500;"
                >
                  Ver Políticas
                </span>
                *
              </span>
            </label>
          </div>

          <PoliticasModal
            v-if="mostrarModalPoliticas"
            :politicas="politicasActivas"
            @close="mostrarModalPoliticas = false"
          />
          
          <button type="submit" class="btn-primary btn-confirmar" :disabled="cargando">
            <span v-if="!cargando">Confirmar Turno</span>
            <span v-else>Procesando...</span>
          </button>
        </form>
      </div>
    </div>

    <!-- Modal de confirmación final -->
    <div v-if="mostrarModalFinal" class="modal-overlay" @click="cerrarModal">
      <div class="modal-content" @click.stop>
        <h2>✅ ¡Turno reservado exitosamente!</h2>
        
        <div v-if="turnoCreado" class="resumen-completo">
          <div class="turno-numero">
            <span class="label">Número de turno:</span>
            <span class="numero">#{{ turnoCreado.id }}</span>
          </div>
          
          <hr />
          
          <p><strong>Servicio:</strong> {{ turnoCreado.servicioNombre }}</p>
          <p><strong>Profesional:</strong> {{ turnoCreado.profesionalNombre }} {{ turnoCreado.profesionalApellido }}</p>
          <p><strong>Fecha:</strong> {{ formatearFechaCompleta(turnoCreado.fecha) }}</p>
          <p><strong>Hora de inicio:</strong> {{ turnoCreado.horaInicio }}</p>
          <p><strong>Duración:</strong> {{ turnoCreado.duracionMinutos }} minutos</p>
          <p><strong>Precio:</strong> ${{ turnoCreado.precio }}</p>
          <p><strong>Estado:</strong> <span class="badge-estado">{{ turnoCreado.estado }}</span></p>
          
          <hr />
          
          <p><strong>Cliente:</strong> {{ turnoCreado.clienteNombre }}</p>
          <p><strong>Teléfono:</strong> {{ turnoCreado.clienteTelefono }}</p>
          <p v-if="turnoCreado.clienteEmail"><strong>Email:</strong> {{ turnoCreado.clienteEmail }}</p>
        </div>
        
        <div v-if="!clienteAutenticado" class="cta-cuenta">
          <div class="cta-icon">🎯</div>
          <h3>¿Querés gestionar tus turnos más fácil?</h3>
          <p>Creá tu cuenta y:</p>
          <ul>
            <li>✓ Visualizá todos tus turnos en un solo lugar</li>
            <li>✓ Cancelá o modificá turnos cuando quieras</li>
            <li>✓ Recibí recordatorios automáticos</li>
            <li>✓ Accedé a tu historial completo</li>
          </ul>
          <button class="btn-secondary" @click="irCrearCuenta">Crear mi cuenta gratis</button>
        </div>
        
        <div class="modal-actions">
          <button class="btn-outline" @click="finalizarYSalir">Finalizar</button>
          <button class="btn-primary" @click="reservarOtroTurno">Reservar otro turno</button>
        </div>
      </div>
    </div>

    <!-- Modal de teléfono registrado (detección pasiva) -->
    <div v-if="mostrarModalTelefonoRegistrado" class="modal-overlay" @click="cerrarModalTelefono">
      <div class="modal-content modal-telefono-registrado" @click.stop>
        <div class="flex items-center mb-4">
          <svg class="w-12 h-12 text-blue-500 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <div>
            <h3 class="text-lg font-bold text-gray-900">Número ya en el sistema</h3>
            <p class="text-sm text-gray-600 mt-1">
              El número {{ telefonoConflicto }} ya existe en nuestra base de datos.
            </p>
          </div>
        </div>

        <div class="mb-3">
          <p class="text-gray-700">
            <template v-if="telefonoInfoData && telefonoInfoData.tieneUsuario">
              Detectamos que este número está asociado a una cuenta registrada{{ telefonoInfoData.nombreEnmascarado ? ' (' + telefonoInfoData.nombreEnmascarado + ')' : '' }}. Por motivos de seguridad no podemos asociar una reserva a una cuenta registrada sin que el titular inicie sesión.
            </template>
            <template v-else>
              Detectamos que este número corresponde a un cliente existente en la empresa{{ telefonoInfoData && telefonoInfoData.nombreEnmascarado ? ' (' + telefonoInfoData.nombreEnmascarado + ')' : '' }}. Si continuás, la reserva se asociará a ese cliente.
            </template>
          </p>
        </div>

        <div class="options-container mb-4">
          <div class="option-card">
            <div class="option-icon">🔐</div>
            <h4 v-if="telefonoInfoData && telefonoInfoData.tieneUsuario">Iniciar Sesión (Recomendado)</h4>
            <h4 v-else>Completar registro</h4>
            <p v-if="telefonoInfoData && telefonoInfoData.tieneUsuario">Ingresá para gestionar tus turnos y ver tu historial.</p>
            <p v-else>Completá tus datos para asociar la reserva a tu perfil y acceder a tu historial.</p>
          </div>
          <div v-if="!clienteAutenticado && (!telefonoInfoData || !telefonoInfoData.tieneUsuario)" class="option-card">
            <div class="option-icon">🔗</div>
            <h4>Continuar y asociar</h4>
            <p>Reservá ahora: la reserva se vinculará al cliente existente con este teléfono.</p>
          </div>
        </div>

          <div class="flex gap-3">
          <button 
            v-if="telefonoInfoData && telefonoInfoData.tieneUsuario"
            @click="irALoginDesdeConflicto"
            class="flex-1 bg-blue-600 text-white px-4 py-3 rounded-md hover:bg-blue-700 font-medium transition-colors shadow-sm"
          >
            Iniciar Sesión
          </button>
          <button 
            v-else-if="!clienteAutenticado"
            @click="irARegistroDesdeConflicto"
            class="flex-1 bg-blue-600 text-white px-4 py-3 rounded-md hover:bg-blue-700 font-medium transition-colors shadow-sm"
          >
            Completar registro
          </button>

          <button 
            v-if="!clienteAutenticado && (!telefonoInfoData || !telefonoInfoData.tieneUsuario)"
            @click="continuarYAsociar"
            class="flex-1 bg-gray-200 text-gray-700 px-4 py-3 rounded-md hover:bg-gray-300 font-medium transition-colors"
          >
            Continuar y asociar
          </button>
          <button 
            v-else-if="!clienteAutenticado && telefonoInfoData && telefonoInfoData.tieneUsuario"
            disabled
            title="Debés iniciar sesión para asociar este número a una cuenta registrada."
            class="flex-1 bg-gray-100 text-gray-400 px-4 py-3 rounded-md font-medium"
          >
            Asociar (requiere login)
          </button>
           <button 
            v-else-if="clienteAutenticado"
            disabled
            title="No podés asociar a una cuenta que ya está iniciada."
            class="flex-1 bg-gray-100 text-gray-400 px-4 py-3 rounded-md font-medium"
          >
            Asociar (ya conectado)
          </button>
        </div>

        <button 
          @click="cerrarModalTelefono"
          class="mt-3 w-full text-gray-500 hover:text-gray-700 text-sm underline"
        >
          Usar otro número / Cancelar
        </button>
      </div>
    </div>
    </main>

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
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRoute, useRouter } from 'vue-router'
import { useClienteStore } from '@/stores/cliente'
import api from '@/services/api'
import publicoService, { 
  type EmpresaPublica, 
  type ServicioPublico, 
  type ProfesionalPublico,
  type SlotDisponible,
  type CrearTurnoRequest,
  type TurnoResponse
} from '@/services/publico'
import Footer from '@/components/Footer.vue'
import PoliticasModal from '@/components/PoliticasModal.vue'
import PoliticasService from '@/services/politicasCancelacion'
import type { PoliticaCancelacion } from '@/types/politicasCancelacion'

const route = useRoute()
const router = useRouter()
const clienteStore = useClienteStore()

// Estado para el modal de políticas
const mostrarModalPoliticas = ref(false)
const politicasActivas = ref<PoliticaCancelacion[]>([])

async function abrirModalPoliticas() {
  if (!empresaSlug.value) {
    console.error('[Politicas] No hay slug de empresa, no se puede buscar políticas')
    politicasActivas.value = []
    mostrarModalPoliticas.value = true
    return
  }
  try {
    const politicas = await publicoService.obtenerPoliticasCancelacionActivas(empresaSlug.value)
    console.log('[Politicas] Respuesta backend:', politicas)
    if (!Array.isArray(politicas)) {
      console.error('[Politicas] El formato de la respuesta no es un array:', politicas)
      politicasActivas.value = []
      mostrarModalPoliticas.value = true
      return
    }
    if (politicas.length === 0) {
      console.warn('[Politicas] El backend devolvió un array vacío de políticas activas')
    }
    politicasActivas.value = politicas
    mostrarModalPoliticas.value = true
  } catch (e) {
    console.error('[Politicas] Error al obtener políticas activas:', e)
    politicasActivas.value = []
    mostrarModalPoliticas.value = true
  }
}

// Estado
const empresaSlug = ref(route.params.empresaSlug as string)
const empresa = ref<EmpresaPublica | null>(null)
const empresaIdActual = ref<number | null>(null)
const empresaCargada = ref(false)
const pasoActual = ref(1)

// Paso 1: Servicios
const servicios = ref<ServicioPublico[]>([])
const servicioSeleccionado = ref<ServicioPublico | null>(null)
const cargandoServicios = ref(false)

// Paso 2: Profesionales
const profesionales = ref<ProfesionalPublico[]>([])
const profesionalSeleccionado = ref<ProfesionalPublico | null>(null)
const cargandoProfesionales = ref(false)

// Paso 3: Fecha
const mesActual = ref(new Date().getMonth())
const añoActual = ref(new Date().getFullYear())
const fechaSeleccionada = ref<Date | null>(null)

// Paso 4: Slots
const slots = ref<SlotDisponible[]>([])
const slotSeleccionado = ref<SlotDisponible | null>(null)
const cargandoSlots = ref(false)

// Paso 5: Datos cliente
const datosCliente = ref({
  nombre: '',
  telefono: '',
  email: '',
  aceptaCondiciones: false
})

const mostrarModalFinal = ref(false)
const turnoCreado = ref<TurnoResponse | null>(null)
const cargando = ref(false)
const mensajeError = ref<string | null>(null)

// Modal de teléfono registrado
const mostrarModalTelefonoRegistrado = ref(false)
const telefonoConflicto = ref('')
const telefonoInfoData = ref<{ existe: boolean; tieneUsuario: boolean; nombreEnmascarado?: string } | null>(null)

// Computed
const diasSemana = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb']
const mesesNombre = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
]

const mesActualNombre = computed(() => mesesNombre[mesActual.value])

const diasMes = computed(() => {
  const primerDia = new Date(añoActual.value, mesActual.value, 1)
  const ultimoDia = new Date(añoActual.value, mesActual.value + 1, 0)
  const diasEnMes = ultimoDia.getDate()
  const primerDiaSemana = primerDia.getDay()

  const dias: Array<{ numero: number | null; fecha: string | null; disabled: boolean; esHoy: boolean }> = []

  // Días vacíos al inicio
  for (let i = 0; i < primerDiaSemana; i++) {
    dias.push({ numero: null, fecha: null, disabled: true, esHoy: false })
  }

  // Días del mes
  const hoy = new Date()
  hoy.setHours(0, 0, 0, 0)
  const maxDias = empresa.value?.diasMaximosReserva || 30
  const fechaMaxima = new Date()
  fechaMaxima.setDate(fechaMaxima.getDate() + maxDias)
  fechaMaxima.setHours(0, 0, 0, 0)

  for (let dia = 1; dia <= diasEnMes; dia++) {
    const fecha = new Date(añoActual.value, mesActual.value, dia)
    fecha.setHours(0, 0, 0, 0)
    const fechaStr = formatearFechaISO(fecha)
    const esHoy = fecha.getTime() === hoy.getTime()
    const disabled = fecha < hoy || fecha > fechaMaxima

    dias.push({ numero: dia, fecha: fechaStr, disabled, esHoy })
  }

  return dias
})

const puedeMesAnterior = computed(() => {
  const primeroDelMes = new Date(añoActual.value, mesActual.value, 1)
  const hoy = new Date()
  hoy.setHours(0, 0, 0, 0)
  return primeroDelMes >= hoy // Permite ir al mes anterior si el mes actual es el primero posible
})


const puedeMesSiguiente = computed(() => {
  const maxDias = empresa.value?.diasMaximosReserva || 30
  const ultimoDelMes = new Date(añoActual.value, mesActual.value + 1, 0)
  const fechaMaxima = new Date()
  fechaMaxima.setDate(fechaMaxima.getDate() + maxDias)
  fechaMaxima.setHours(0,0,0,0) // Asegurarse de comparar solo la fecha
  
  // Si el último día del mes actual es anterior o igual a la fecha máxima permitida
  return ultimoDelMes <= fechaMaxima
})

const fechaSeleccionadaFormateada = computed(() => {
  if (!fechaSeleccionada.value) return ''
  return fechaSeleccionada.value.toLocaleDateString('es-AR', { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })
})

// Métodos
onMounted(async () => {
  try {
    empresa.value = await publicoService.obtenerEmpresa(empresaSlug.value)
    empresaCargada.value = true
    empresaIdActual.value = empresa.value.id
    
    // Intentar recuperar sesión activa de CLIENTE
    try {
      const response = await api.obtenerPerfilCliente()
      console.debug('[ReservarView] obtenerPerfilCliente response:', response)
      // Solo setear cliente si la respuesta es 200, payload válido y pertenece a esta empresa
      if (response && response.status === 200) {
        const data = response.data?.datos ?? response.data
        if (data && (data.id !== undefined || data.empresaId !== undefined || data.telefono !== undefined)) {
          try {
            // Verificar que el perfil recuperado corresponde a la empresa actual
            if (data.empresaId === empresa.value.id || data.empresaSlug === empresa.value.slug) {
              clienteStore.setCliente(data)
              console.debug('[ReservarView] cliente seteado en store:', clienteStore.cliente)
              // Precargar datos del cliente en el formulario
              datosCliente.value.nombre = data.nombre || ''
              datosCliente.value.telefono = data.telefono || ''
              datosCliente.value.email = data.email || ''
            } else {
              // Cliente pertenece a otra empresa, logout
              clienteStore.logout()
            }
          } catch (e) {
            // Si por alguna razón no tenemos empresa cargada, logout
            clienteStore.logout()
          }
        } else {
          // Payload inválido, logout
          clienteStore.logout()
        }
      } else {
        // No hay sesión, logout
        clienteStore.logout()
      }
    } catch (error) {
      // Error al obtener perfil, logout
      console.debug('[ReservarView] Error al obtener perfil, logout:', error)
      clienteStore.logout()
    }
    
    await cargarServicios()
  } catch (error: any) {
    alert('Error al cargar la empresa: ' + (error.response?.data?.mensaje || error.message))
    router.push('/')
  }
})

async function cargarServicios() {
  try {
    cargandoServicios.value = true
    servicios.value = await publicoService.obtenerServicios(empresaSlug.value)
  } catch (error: any) {
    alert('Error al cargar servicios: ' + (error.response?.data?.mensaje || error.message))
  } finally {
    cargandoServicios.value = false
  }
}

async function seleccionarServicio(servicio: ServicioPublico) {
  servicioSeleccionado.value = servicio
  pasoActual.value = 2
  await cargarProfesionales()
}

async function cargarProfesionales() {
  if (!servicioSeleccionado.value) return
  
  try {
    cargandoProfesionales.value = true
    profesionales.value = await publicoService.obtenerProfesionales(
      empresaSlug.value,
      servicioSeleccionado.value.id
    )
  } catch (error: any) {
    alert('Error al cargar profesionales: ' + (error.response?.data?.mensaje || error.message))
  } finally {
    cargandoProfesionales.value = false
  }
}

function seleccionarProfesional(profesional: ProfesionalPublico) {
  profesionalSeleccionado.value = profesional
  pasoActual.value = 3
}

function mesAnterior() {
  // Validar si podemos retroceder más allá del mes actual (si hoy es el primer día del mes)
  const hoy = new Date()
  hoy.setHours(0,0,0,0)
  const primerDiaDelMesActual = new Date(añoActual.value, mesActual.value, 1)
  
  if (primerDiaDelMesActual <= hoy) return; // Ya estamos en el mes más antiguo posible

  if (mesActual.value === 0) {
    mesActual.value = 11
    añoActual.value--
  } else {
    mesActual.value--
  }
}

function mesSiguiente() {
  const maxDias = empresa.value?.diasMaximosReserva || 30
  const fechaMaxima = new Date()
  fechaMaxima.setDate(fechaMaxima.getDate() + maxDias)
  fechaMaxima.setHours(0,0,0,0) // Asegurarse de comparar solo la fecha

  const ultimoDiaDelMesActual = new Date(añoActual.value, mesActual.value + 1, 0)

  // Si el último día del mes actual ya supera la fecha máxima permitida, no avanzar
  if (ultimoDiaDelMesActual > fechaMaxima) return;

  if (mesActual.value === 11) {
    mesActual.value = 0
    añoActual.value++
  } else {
    mesActual.value++
  }
}


async function seleccionarFecha(fecha: string | null) {
  if (!fecha) return
  // Corregido: crear la fecha en local sin desfase de zona horaria
  // fecha es 'YYYY-MM-DD', separamos y creamos Date con año, mes, día
  const [anio, mes, dia] = fecha.split('-').map(Number)
  if (
    typeof anio !== 'number' || isNaN(anio) ||
    typeof mes !== 'number' || isNaN(mes) ||
    typeof dia !== 'number' || isNaN(dia)
  ) {
    // No se puede crear la fecha, datos inválidos
    return
  }
  fechaSeleccionada.value = new Date(anio, mes - 1, dia, 0, 0, 0, 0)
  pasoActual.value = 4
  await cargarSlots()
}

async function cargarSlots() {
  if (!servicioSeleccionado.value || !profesionalSeleccionado.value || !fechaSeleccionada.value) return
  
  const servicio = servicioSeleccionado.value!
  const profesional = profesionalSeleccionado.value!
  const fecha = fechaSeleccionada.value!
  const fechaISO: string = fecha.toISOString().split('T')[0]!
  
  try {
    cargandoSlots.value = true
    slots.value = await publicoService.obtenerDisponibilidad(
      empresaSlug.value,
      servicio.id,
      profesional.id,
      fechaISO
    )
  } catch (error: any) {
    alert('Error al cargar horarios: ' + (error.response?.data?.mensaje || error.message))
  } finally {
    cargandoSlots.value = false
  }
}

function seleccionarSlot(slot: SlotDisponible) {
  slotSeleccionado.value = slot
  pasoActual.value = 5
}

async function confirmarReserva() {
  // Resetear el mensaje de error
  mensajeError.value = null

  // Si el cliente no está autenticado, realizamos la validación de datos y la detección pasiva de teléfono
  if (!clienteAutenticado.value) {
    // Validar datos del cliente
    if (!datosCliente.value.nombre || !datosCliente.value.telefono || 
        !datosCliente.value.aceptaCondiciones) {
      alert('Por favor completa todos los campos obligatorios para continuar.')
      return
    }

    // Detección pasiva: obtener info amplia del teléfono
    try {
      const info = await publicoService.telefonoInfo(
        empresaSlug.value,
        datosCliente.value.telefono
      )

      if (info && info.existe) {
        telefonoConflicto.value = datosCliente.value.telefono
        telefonoInfoData.value = info
        mostrarModalTelefonoRegistrado.value = true
        return // Esperar decisión del usuario en el modal
      }
    } catch (error) {
      // Si falla la verificación de teléfono (ej. error de red), continuamos asumiendo que no hay conflicto
      console.warn('No se pudo verificar el teléfono, continuando con el proceso de reserva...', error)
    }
  }

  // Si el cliente está autenticado o si la detección de teléfono no arrojó conflicto, procesamos la reserva
  await procesarReserva()
}

async function procesarReserva() {
  try {
    cargando.value = true
    
    if (!servicioSeleccionado.value || !profesionalSeleccionado.value || 
        !fechaSeleccionada.value || !slotSeleccionado.value) {
      alert('Error interno: Faltan datos de reserva.')
      return
    }

    const servicio = servicioSeleccionado.value!
    const profesional = profesionalSeleccionado.value!
    const fecha = fechaSeleccionada.value!
    const slot = slotSeleccionado.value!

    // Formatear fecha a YYYY-MM-DD
    const fechaISO: string = fecha.toISOString().split('T')[0]!

    // Extraer solo la hora y minutos del slot.horaInicio (que ya viene en formato ISO)
    // Asegurarse de que sea HH:MM
    const horaISO: string = slot.horaInicio.substring(11, 16)

    // Preparar el objeto de solicitud para crear el turno
    const request: CrearTurnoRequest = {
      servicioId: servicio.id,
      profesionalId: profesional.id,
      fecha: fechaISO,
      horaInicio: horaISO,
      nombreCliente: clienteAutenticado.value 
        ? clienteStore.cliente!.nombre 
        : datosCliente.value.nombre,
      telefonoCliente: clienteAutenticado.value 
        ? clienteStore.cliente!.telefono 
        : datosCliente.value.telefono,
      emailCliente: clienteAutenticado.value 
        ? clienteStore.cliente!.email 
        : (datosCliente.value.email?.trim() || undefined) // Email opcional
    }

    const respuesta = await publicoService.crearTurno(empresaSlug.value, request)
    turnoCreado.value = respuesta
    
    // Mostrar modal de confirmación con los datos del turno creado
    mostrarModalFinal.value = true
  } catch (err: any) {
    console.error('Error al crear turno:', err)

    // Manejar el error específico de teléfono conflictivo con cuenta registrada
    if (err.response?.status === 409) {
      telefonoConflicto.value = clienteAutenticado.value ? clienteStore.cliente!.telefono : datosCliente.value.telefono
      // Asumimos que tieneUsuario es true porque es un conflicto 409
      telefonoInfoData.value = { existe: true, tieneUsuario: true, nombreEnmascarado: err.response.data?.nombreEnmascarado }
      mostrarModalTelefonoRegistrado.value = true
      return // Detener el flujo hasta que el usuario decida en el modal
    }

    // Para otros errores, mostrar mensaje genérico o específico del backend
    mensajeError.value = err.response?.data?.mensaje || 'Error al crear la reserva. Por favor intenta nuevamente.'
    alert(mensajeError.value)
  } finally {
    cargando.value = false
  }
}

function cerrarModal() {
  mostrarModalFinal.value = false
  // Opcionalmente, podrías redirigir o resetear el formulario aquí
}

function cerrarModalTelefono() {
  mostrarModalTelefonoRegistrado.value = false
  // Limpiar estado del modal
  telefonoConflicto.value = ''
  telefonoInfoData.value = null
}

function irALoginDesdeConflicto() {
  // Redirigir a la página de login de cliente, pasando el número de teléfono
  router.push({
    name: 'LoginCliente',
    params: { empresaSlug: empresaSlug.value },
    query: { 
      redirect: `/reservar/${empresaSlug.value}`, // Volver a este componente después del login
      telefono: telefonoConflicto.value
    }
  })
}

function irARegistroDesdeConflicto() {
  // Redirigir a la página de registro de cliente, pasando el número de teléfono
  router.push({
    name: 'RegistroCliente',
    params: { empresaSlug: empresaSlug.value },
    query: { telefono: telefonoConflicto.value }
  })
}

function continuarYAsociar() {
  // Cerrar el modal y proceder con la reserva.
  // El backend se encargará de asociar la reserva al cliente existente.
  cerrarModalTelefono()
  procesarReserva() // Vuelve a intentar la reserva
}

function finalizarYSalir() {
  mostrarModalFinal.value = false
  // El usuario puede querer ir a "Mis Turnos" o al login.
  // Redirigir al login por ahora.
  router.push({
    name: 'LoginCliente',
    params: { empresaSlug: empresaSlug.value }
  })
}

function reservarOtroTurno() {
  // Resetear todo el estado del componente para iniciar una nueva reserva
  mostrarModalFinal.value = false
  servicioSeleccionado.value = null
  profesionalSeleccionado.value = null
  fechaSeleccionada.value = null
  slotSeleccionado.value = null
  slots.value = []
  profesionales.value = []
  datosCliente.value = { // Resetear campos de invitado
    nombre: '',
    telefono: '',
    email: '',
    aceptaCondiciones: false
  }
  turnoCreado.value = null
  pasoActual.value = 1 // Volver al primer paso

  // Scroll suave al principio de la página
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function irCrearCuenta() {
  // Si tenemos datos del cliente (sea del login o del formulario), pre-cargarlos en el registro
  const telefonoParaRegistro = datosCliente.value.telefono || clienteStore.cliente?.telefono || ''
  
  router.push({
    name: 'RegistroCliente',
    params: { empresaSlug: empresaSlug.value },
    query: { telefono: telefonoParaRegistro } // Pasar el teléfono si está disponible
  })
  cerrarModal() // Cerrar el modal de confirmación
}

function volverPaso(paso: number) {
  pasoActual.value = paso
}

function formatearFechaISO(fecha: Date): string {
  const año = fecha.getFullYear()
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${año}-${mes}-${dia}`
}

function formatearFechaCompleta(fechaISO: string): string {
  // Asegurarse de que la fecha ISO sea procesada correctamente, añadiendo hora 00:00:00
  const fecha = new Date(fechaISO + 'T00:00:00')
  const dia = fecha.getDate()
  const mes = mesesNombre[fecha.getMonth()]
  const año = fecha.getFullYear()
  const diaSemana = diasSemana[fecha.getDay()]
  return `${diaSemana} ${dia} de ${mes} ${año}`
}

function formatearHora(isoString: string): string {
  // La hora ya viene en formato ISO (ej: "2023-10-27T10:00:00")
  // Queremos extraer HH:MM
  try {
    const fecha = new Date(isoString);
    return fecha.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit', hour12: false })
  } catch (e) {
    console.error("Error formateando hora:", isoString, e);
    return isoString; // Devolver el string original si hay error
  }
}

// Computed para verificar si el cliente está autenticado Y pertenece a la empresa actual
const clienteValido = computed(() => {
  if (!clienteStore.isAuthenticated) return false
  if (!empresaCargada.value || !empresa.value) return false // Asegurarse de que la empresa esté cargada
  const clienteEmpresaId = clienteStore.cliente?.empresaId
  // Verificar que el cliente tenga una empresa asignada y que coincida con la empresa actual
  return clienteEmpresaId !== undefined && clienteEmpresaId !== null && clienteEmpresaId === empresa.value.id
})

// Indica si el cliente está autenticado Y su cuenta está asociada a la empresa actual
const clienteAutenticado = computed(() => clienteStore.isAuthenticated && clienteValido.value)

</script>

<style scoped>
/* Estilos completamente responsive y mobile-first */

.paso-content {
  background: white;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

@media (min-width: 640px) {
  .paso-content {
    padding: 24px;
  }
}

@media (min-width: 1024px) {
  .paso-content {
    padding: 30px;
  }
}

.paso-content h2 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 700;
}

@media (min-width: 640px) {
  .paso-content h2 {
    margin-bottom: 20px;
    font-size: 24px;
  }
}

.btn-back {
  margin-bottom: 16px;
  touch-action: manipulation;
}

@media (min-width: 640px) {
  .btn-back {
    margin-bottom: 20px;
  }
}

.loading, .empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #666;
}

/* Servicios Grid - responsive para móvil */
.servicios-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

@media (min-width: 640px) {
  .servicios-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}

@media (min-width: 1024px) {
  .servicios-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }
}

.servicio-card {
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
}

@media (min-width: 640px) {
  .servicio-card {
    padding: 20px;
  }
}

.servicio-card:hover, .servicio-card:active {
  border-color: #007bff;
  box-shadow: 0 4px 12px rgba(0,123,255,0.2);
  transform: translateY(-2px);
}

.servicio-card h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

@media (min-width: 640px) {
  .servicio-card h3 {
    margin-bottom: 10px;
    font-size: 20px;
  }
}

.servicio-card .descripcion {
  color: #666;
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.4;
}

.servicio-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  gap: 8px;
}

.duracion {
  color: #666;
  font-size: 14px;
}

.precio {
  font-size: 18px;
  color: #28a745;
  white-space: nowrap;
}

@media (min-width: 640px) {
  .precio {
    font-size: 20px;
  }
}

/* Profesionales List - mejor para móvil */
.profesionales-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

@media (min-width: 640px) {
  .profesionales-list {
    gap: 15px;
  }
}

.profesional-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
}

@media (min-width: 640px) {
  .profesional-card {
    padding: 20px;
  }
}

.profesional-card:hover, .profesional-card:active {
  border-color: #007bff;
  box-shadow: 0 4px 12px rgba(0,123,255,0.2);
}

.profesional-info h3 {
  margin: 0 0 6px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

@media (min-width: 640px) {
  .profesional-info h3 {
    margin-bottom: 8px;
    font-size: 18px;
  }
}

.profesional-info p {
  margin: 0;
  color: #666;
  font-size: 13px;
  line-height: 1.4;
}

@media (min-width: 640px) {
  .profesional-info p {
    font-size: 14px;
  }
}

.arrow {
  font-size: 20px;
  color: #007bff;
  flex-shrink: 0;
  margin-left: 8px;
}

@media (min-width: 640px) {
  .arrow {
    font-size: 24px;
  }
}

/* Calendario completamente responsive y mobile-first */
.calendario-container {
  width: 100%;
  max-width: 100%;
  margin: 0 auto;
}

@media (min-width: 640px) {
  .calendario-container {
    max-width: 500px;
  }
}

@media (min-width: 1024px) {
  .calendario-container {
    max-width: 600px;
  }
}

.calendario-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}

@media (min-width: 640px) {
  .calendario-header {
    margin-bottom: 20px;
  }
}

.calendario-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  text-align: center;
  flex: 1;
}

@media (min-width: 640px) {
  .calendario-header h3 {
    font-size: 18px;
  }
}

.calendario-header button {
  background: #007bff;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 16px;
  min-width: 40px;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
  transition: background 0.2s;
}

@media (min-width: 640px) {
  .calendario-header button {
    padding: 8px 16px;
    font-size: 18px;
  }
}

.calendario-header button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.calendario-header button:not(:disabled):active {
  background: #0056b3;
}

.calendario-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  width: 100%;
}

@media (min-width: 640px) {
  .calendario-grid {
    gap: 6px;
  }
}

@media (min-width: 768px) {
  .calendario-grid {
    gap: 8px;
  }
}

.dia-header {
  text-align: center;
  font-weight: 600;
  padding: 6px 2px;
  color: #666;
  font-size: 11px;
}

@media (min-width: 640px) {
  .dia-header {
    padding: 8px 4px;
    font-size: 12px;
  }
}

@media (min-width: 768px) {
  .dia-header {
    padding: 10px;
    font-size: 14px;
  }
}

.dia-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 600;
  font-size: 13px;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
  min-height: 40px;
}

@media (min-width: 640px) {
  .dia-cell {
    font-size: 14px;
    min-height: 48px;
  }
}

@media (min-width: 768px) {
  .dia-cell {
    font-size: 16px;
    border-radius: 10px;
  }
}

.dia-cell:not(.disabled):hover,
.dia-cell:not(.disabled):active {
  border-color: #007bff;
  background: #e7f3ff;
}

.dia-cell.disabled {
  color: #ccc;
  cursor: not-allowed;
  background: #f8f9fa;
}

.dia-cell.selected {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.dia-cell.hoy {
  border-color: #28a745;
  font-weight: bold;
}

/* Slots List - grid responsive para móvil */
.fecha-seleccionada {
  text-align: center;
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 16px;
  line-height: 1.4;
}

@media (min-width: 640px) {
  .fecha-seleccionada {
    font-size: 17px;
    margin-bottom: 20px;
  }
}

.slots-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

@media (min-width: 480px) {
  .slots-list {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
}

@media (min-width: 640px) {
  .slots-list {
    grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
    gap: 15px;
  }
}

.slot-btn {
  padding: 14px 10px;
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  background: white;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.2s;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
}

@media (min-width: 640px) {
  .slot-btn {
    padding: 15px;
    font-size: 16px;
  }
}

.slot-btn:hover, .slot-btn:active {
  border-color: #007bff;
  background: #e7f3ff;
  transform: translateY(-2px);
}

/* Resumen - mejor lectura en móvil */
.resumen-reserva {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 10px;
  margin-bottom: 20px;
}

@media (min-width: 640px) {
  .resumen-reserva {
    padding: 20px;
    margin-bottom: 30px;
  }
}

.resumen-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
  gap: 12px;
  font-size: 14px;
}

@media (min-width: 640px) {
  .resumen-item {
    font-size: 16px;
  }
}

.resumen-item strong {
  flex-shrink: 0;
  min-width: 90px;
}

.resumen-item span {
  text-align: right;
  word-break: break-word;
}

.resumen-item:last-child {
  border-bottom: none;
}

/* Formulario optimizado para móvil */
.formulario-cliente {
  margin-top: 20px;
}

@media (min-width: 640px) {
  .formulario-cliente {
    margin-top: 30px;
  }
}

.formulario-cliente h3 {
  margin-bottom: 16px;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

@media (min-width: 640px) {
  .formulario-cliente h3 {
    margin-bottom: 20px;
    font-size: 20px;
  }
}

.form-group {
  margin-bottom: 14px;
}

@media (min-width: 640px) {
  .form-group {
    margin-bottom: 16px;
  }
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

@media (min-width: 640px) {
  .form-group label {
    font-size: 15px;
  }
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  touch-action: manipulation;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #007bff;
}

.checkbox-group {
  margin-top: 20px;
  margin-bottom: 20px;
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  cursor: pointer;
  font-size: 14px;
  line-height: 1.5;
}

@media (min-width: 640px) {
  .checkbox-label {
    font-size: 15px;
  }
}

.checkbox-label input[type="checkbox"] {
  margin-top: 2px;
  width: 18px;
  height: 18px;
  cursor: pointer;
  flex-shrink: 0;
}

.politicas-link-azul {
  color: #1976d2;
  text-decoration: underline;
  cursor: pointer;
  font-weight: 500;
  margin-left: 4px;
}

.info-text {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 8px;
  padding: 12px;
  margin: 16px 0;
  color: #856404;
  font-size: 14px;
  line-height: 1.5;
}

@media (min-width: 640px) {
  .info-text {
    padding: 15px;
  }
}

/* Botones optimizados para mobile-first */
.btn-primary, .btn-secondary {
  padding: 14px 20px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
}

@media (min-width: 640px) {
  .btn-primary, .btn-secondary {
    padding: 14px 24px;
  }
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover, .btn-primary:active {
  background: #0056b3;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover, .btn-secondary:active {
  background: #545b62;
}

.btn-outline {
  background: transparent;
  color: #6c757d;
  border: 2px solid #6c757d;
  padding: 12px 18px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.2s ease;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
}

@media (min-width: 640px) {
  .btn-outline {
    padding: 12px 20px;
    font-size: 16px;
  }
}

.btn-outline:hover, .btn-outline:active {
  background: #6c757d;
  color: white;
}

.modal-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

@media (min-width: 640px) {
  .modal-actions {
    flex-direction: row;
    gap: 15px;
    justify-content: center;
  }
}

.btn-confirmar {
  width: 100%;
  padding: 16px;
  font-size: 17px;
}

@media (min-width: 640px) {
  .btn-confirmar {
    padding: 16px;
    font-size: 18px;
  }
}

/* Modal responsive para móvil */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 16px;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 12px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

@media (min-width: 640px) {
  .modal-content {
    padding: 30px;
    max-height: 85vh;
  }
}

.modal-content h2 {
  margin-top: 0;
  color: #28a745;
  font-size: 20px;
  line-height: 1.3;
}

@media (min-width: 640px) {
  .modal-content h2 {
    font-size: 24px;
  }
}

.resumen-completo {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 10px;
  margin: 16px 0;
}

@media (min-width: 640px) {
  .resumen-completo {
    padding: 20px;
    margin: 20px 0;
  }
}

.resumen-completo p {
  margin: 8px 0;
  font-size: 14px;
}

@media (min-width: 640px) {
  .resumen-completo p {
    margin: 10px 0;
    font-size: 15px;
  }
}

.resumen-completo hr {
  margin: 12px 0;
  border: none;
  border-top: 2px solid #e0e0e0;
}

@media (min-width: 640px) {
  .resumen-completo hr {
    margin: 15px 0;
  }
}

.turno-numero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 14px;
  border-radius: 10px;
  text-align: center;
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

@media (min-width: 640px) {
  .turno-numero {
    padding: 15px;
    margin-bottom: 20px;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
}

.turno-numero .label {
  font-size: 13px;
  font-weight: normal;
}

@media (min-width: 640px) {
  .turno-numero .label {
    font-size: 14px;
  }
}

.turno-numero .numero {
  font-size: 22px;
  font-weight: bold;
}

@media (min-width: 640px) {
  .turno-numero .numero {
    font-size: 24px;
  }
}

.badge-estado {
  background: #28a745;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: bold;
  text-transform: uppercase;
  display: inline-block;
}

@media (min-width: 640px) {
  .badge-estado {
    font-size: 12px;
  }
}

.info-box {
  background: #e3f2fd;
  border-left: 4px solid #2196f3;
  padding: 12px;
  border-radius: 6px;
  margin: 16px 0;
}

@media (min-width: 640px) {
  .info-box {
    padding: 15px;
    margin: 20px 0;
  }
}

.info-text {
  margin: 0;
  color: #1976d2;
  font-size: 13px;
  line-height: 1.5;
}

@media (min-width: 640px) {
  .info-text {
    font-size: 14px;
  }
}

.cta-cuenta {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  padding: 20px;
  border-radius: 12px;
  margin: 16px 0;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

@media (min-width: 640px) {
  .cta-cuenta {
    padding: 25px;
    margin: 20px 0;
  }
}

.cta-icon {
  font-size: 40px;
  margin-bottom: 8px;
}

@media (min-width: 640px) {
  .cta-icon {
    font-size: 48px;
    margin-bottom: 10px;
  }
}

.cta-cuenta h3 {
  color: #2d3436;
  margin: 8px 0;
  font-size: 18px;
  line-height: 1.3;
}

@media (min-width: 640px) {
  .cta-cuenta h3 {
    margin: 10px 0;
    font-size: 20px;
  }
}

.cta-cuenta p {
  color: #636e72;
  margin: 8px 0;
  font-size: 14px;
}

@media (min-width: 640px) {
  .cta-cuenta p {
    margin: 10px 0;
    font-size: 16px;
  }
}

.cta-cuenta ul {
  list-style: none;
  padding: 0;
  margin: 12px 0;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

@media (min-width: 640px) {
  .cta-cuenta ul {
    margin: 15px auto;
  }
}

.cta-cuenta li {
  padding: 6px 0;
  color: #2d3436;
  font-size: 13px;
}

@media (min-width: 640px) {
  .cta-cuenta li {
    padding: 8px 0;
    font-size: 14px;
  }
}

.cta-cuenta .btn-secondary {
  background: #6c5ce7;
  color: white;
  border: none;
  padding: 14px 24px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 15px;
  font-weight: bold;
  margin-top: 12px;
  transition: all 0.2s ease;
  width: 100%;
}

@media (min-width: 480px) {
  .cta-cuenta .btn-secondary {
    width: auto;
  }
}

@media (min-width: 640px) {
  .cta-cuenta .btn-secondary {
    padding: 14px 30px;
    font-size: 16px;
    margin-top: 15px;
  }
}

.cta-cuenta .btn-secondary:hover,
.cta-cuenta .btn-secondary:active {
  background: #5f4dd1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.4);
}

.advertencia {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
  padding: 12px;
  border-radius: 8px;
  margin: 16px 0;
  font-size: 14px;
  line-height: 1.5;
}

@media (min-width: 640px) {
  .advertencia {
    padding: 15px;
    margin: 20px 0;
  }
}

/* Modal de teléfono registrado - responsive */
.modal-telefono-registrado {
  max-width: 550px;
}

.modal-telefono-registrado .flex {
  display: flex;
}

.modal-telefono-registrado .items-center {
  align-items: center;
}

.modal-telefono-registrado .mb-4 {
  margin-bottom: 1rem;
}

.modal-telefono-registrado .mb-3 {
  margin-bottom: 0.75rem;
}

.modal-telefono-registrado .mr-3 {
  margin-right: 0.75rem;
}

.modal-telefono-registrado .mt-1 {
  margin-top: 0.25rem;
}

.modal-telefono-registrado .w-12 {
  width: 3rem;
  flex-shrink: 0;
}

.modal-telefono-registrado .h-12 {
  height: 3rem;
}

.modal-telefono-registrado .text-blue-500 {
  color: #3b82f6;
}

.modal-telefono-registrado .text-lg {
  font-size: 1.125rem;
  line-height: 1.3;
}

.modal-telefono-registrado .text-sm {
  font-size: 0.875rem;
  line-height: 1.4;
}

.modal-telefono-registrado .text-gray-900 {
  color: #111827;
}

.modal-telefono-registrado .text-gray-700 {
  color: #374151;
}

.modal-telefono-registrado .text-gray-600 {
  color: #4b5563;
}

.modal-telefono-registrado .text-gray-500 {
  color: #6b7280;
}

.modal-telefono-registrado .text-gray-400 {
  color: #9ca3af;
}

.modal-telefono-registrado .font-bold {
  font-weight: 700;
}

.modal-telefono-registrado .font-medium {
  font-weight: 500;
}

.options-container {
  display: grid;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

@media (min-width: 640px) {
  .options-container {
    margin-bottom: 1.5rem;
  }
}

.option-card {
  background: #f9fafb;
  border: 2px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px;
  transition: all 0.2s;
}

@media (min-width: 640px) {
  .option-card {
    padding: 1rem;
  }
}

.option-card:hover {
  border-color: #3b82f6;
  background: #eff6ff;
}

.option-icon {
  font-size: 1.75rem;
  margin-bottom: 0.5rem;
}

@media (min-width: 640px) {
  .option-icon {
    font-size: 2rem;
  }
}

.option-card h4 {
  font-size: 0.95rem;
  font-weight: 600;
  color: #111827;
  margin: 0.5rem 0;
  line-height: 1.3;
}

@media (min-width: 640px) {
  .option-card h4 {
    font-size: 1rem;
  }
}

.option-card p {
  font-size: 0.8rem;
  color: #6b7280;
  margin: 0;
  line-height: 1.4;
}

@media (min-width: 640px) {
  .option-card p {
    font-size: 0.875rem;
  }
}

.modal-telefono-registrado .gap-3 {
  gap: 0.75rem;
}

.modal-telefono-registrado .flex-1 {
  flex: 1;
  min-width: 0;
}

.modal-telefono-registrado .bg-blue-600 {
  background-color: #2563eb;
}

.modal-telefono-registrado .text-white {
  color: white;
}

.modal-telefono-registrado .px-4 {
  padding-left: 1rem;
  padding-right: 1rem;
}

.modal-telefono-registrado .py-3 {
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
}

.modal-telefono-registrado .rounded-md {
  border-radius: 0.5rem;
}

.modal-telefono-registrado .hover\:bg-blue-700:hover {
  background-color: #1d4ed8;
}

.modal-telefono-registrado .bg-gray-200 {
  background-color: #e5e7eb;
}

.modal-telefono-registrado .bg-gray-100 {
  background-color: #f3f4f6;
}

.modal-telefono-registrado .hover\:bg-gray-300:hover {
  background-color: #d1d5db;
}

.modal-telefono-registrado .shadow-sm {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.modal-telefono-registrado .transition-colors {
  transition-property: color, background-color, border-color;
  transition-duration: 200ms;
}

.modal-telefono-registrado .mt-3 {
  margin-top: 0.75rem;
}

.modal-telefono-registrado .w-full {
  width: 100%;
}

.modal-telefono-registrado .hover\:text-gray-700:hover {
  color: #374151;
}

.modal-telefono-registrado .underline {
  text-decoration: underline;
}

/* Estilos para prevenir zoom en iOS al hacer focus en inputs */
@supports (-webkit-touch-callout: none) {
  .form-group input,
  .form-group select,
  .form-group textarea {
    font-size: 16px;
  }
}
</style>
