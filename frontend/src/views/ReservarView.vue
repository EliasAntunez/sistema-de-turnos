<template>
  <div class="reservar-container">
    <!-- Header empresa -->
    <div v-if="empresa" class="empresa-header">
      <h1>{{ empresa.nombre }}</h1>
      <p v-if="empresa.descripcion">{{ empresa.descripcion }}</p>
      <div class="empresa-info">
        <span v-if="empresa.ciudad">📍 {{ empresa.ciudad }}</span>
        <span v-if="empresa.telefono">📞 {{ empresa.telefono }}</span>
      </div>
    </div>

    <!-- Indicador de pasos -->
    <div class="stepper">
      <div :class="['step', { active: pasoActual === 1, completed: pasoActual > 1 }]">
        <span class="step-number">1</span>
        <span class="step-label">Servicio</span>
      </div>
      <div :class="['step', { active: pasoActual === 2, completed: pasoActual > 2 }]">
        <span class="step-number">2</span>
        <span class="step-label">Profesional</span>
      </div>
      <div :class="['step', { active: pasoActual === 3, completed: pasoActual > 3 }]">
        <span class="step-number">3</span>
        <span class="step-label">Fecha</span>
      </div>
      <div :class="['step', { active: pasoActual === 4, completed: pasoActual > 4 }]">
        <span class="step-number">4</span>
        <span class="step-label">Hora</span>
      </div>
      <div :class="['step', { active: pasoActual === 5 }]">
        <span class="step-number">5</span>
        <span class="step-label">Confirmar</span>
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

      <!-- Formulario de datos del cliente (si no está logueado) -->
      <div v-if="!usuarioLogueado" class="formulario-cliente">
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
              <span>Acepto la política de cancelación e inasistencias *</span>
            </label>
          </div>
          
          <p class="info-text">
            ℹ️ Esta información es solo para mostrar el resumen. 
            La reserva real se habilitará cuando implementemos el sistema de turnos.
          </p>
          
          <button type="submit" class="btn-primary btn-confirmar">
            Ver resumen final
          </button>
        </form>
      </div>
      
      <div v-else class="resumen-final">
        <p class="info-text">
          ℹ️ La reserva real se habilitará cuando implementemos el sistema de turnos.
        </p>
        <button class="btn-primary btn-confirmar" @click="confirmarReserva">
          Ver resumen final
        </button>
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
        
        <div class="info-box">
          <p class="info-text">
            📱 Recibirás una confirmación por WhatsApp/SMS al número registrado.
          </p>
        </div>
        
        <div class="cta-cuenta">
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
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import publicoService, { 
  type EmpresaPublica, 
  type ServicioPublico, 
  type ProfesionalPublico,
  type SlotDisponible,
  type CrearTurnoRequest,
  type TurnoResponse
} from '@/services/publico'

const route = useRoute()
const router = useRouter()

// Estado
const empresaSlug = ref(route.params.empresaSlug as string)
const empresa = ref<EmpresaPublica | null>(null)
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
const usuarioLogueado = ref(false) // TODO: integrar con auth store
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
  return primeroDelMes > hoy
})

const puedeMesSiguiente = computed(() => {
  const maxDias = empresa.value?.diasMaximosReserva || 30
  const ultimoDelMes = new Date(añoActual.value, mesActual.value + 1, 0)
  const fechaMaxima = new Date()
  fechaMaxima.setDate(fechaMaxima.getDate() + maxDias)
  return ultimoDelMes < fechaMaxima
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
  if (mesActual.value === 0) {
    mesActual.value = 11
    añoActual.value--
  } else {
    mesActual.value--
  }
}

function mesSiguiente() {
  if (mesActual.value === 11) {
    mesActual.value = 0
    añoActual.value++
  } else {
    mesActual.value++
  }
}

async function seleccionarFecha(fecha: string | null) {
  if (!fecha) return
  fechaSeleccionada.value = new Date(fecha + 'T00:00:00')
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
  if (!usuarioLogueado.value) {
    // Validar datos del cliente
    if (!datosCliente.value.nombre || !datosCliente.value.telefono || 
        !datosCliente.value.aceptaCondiciones) {
      alert('Por favor completa todos los campos obligatorios')
      return
    }
  }

  // Crear turno real
  try {
    cargando.value = true
    mensajeError.value = null

    if (!servicioSeleccionado.value || !profesionalSeleccionado.value || 
        !fechaSeleccionada.value || !slotSeleccionado.value) {
      alert('Datos de reserva incompletos')
      return
    }

    const servicio = servicioSeleccionado.value!
    const profesional = profesionalSeleccionado.value!
    const fecha = fechaSeleccionada.value!
    const slot = slotSeleccionado.value!

    const fechaISO: string = fecha.toISOString().split('T')[0]!
    const horaPartes = slot.horaInicio.split('T')
    const horaISO: string = (horaPartes[1]?.substring(0, 5) || slot.horaInicio.substring(0, 5))

    const request: CrearTurnoRequest = {
      servicioId: servicio.id,
      profesionalId: profesional.id,
      fecha: fechaISO,
      horaInicio: horaISO,
      nombreCliente: datosCliente.value.nombre,
      telefonoCliente: datosCliente.value.telefono,
      emailCliente: datosCliente.value.email?.trim() || undefined
    }

    const respuesta = await publicoService.crearTurno(empresaSlug.value, request)
    turnoCreado.value = respuesta
    
    // Mostrar modal de confirmación con datos reales
    mostrarModalFinal.value = true
  } catch (err: any) {
    console.error('Error al crear turno:', err)
    mensajeError.value = err.response?.data?.mensaje || 'Error al crear la reserva. Por favor intenta nuevamente.'
    alert(mensajeError.value)
  } finally {
    cargando.value = false
  }
}

function cerrarModal() {
  mostrarModalFinal.value = false
}

function finalizarYSalir() {
  mostrarModalFinal.value = false
  // Redirigir al home
  router.push('/')
}

function reservarOtroTurno() {
  // Resetear formulario pero mantener la empresa
  mostrarModalFinal.value = false
  servicioSeleccionado.value = null
  profesionalSeleccionado.value = null
  fechaSeleccionada.value = null
  slotSeleccionado.value = null
  slots.value = []
  profesionales.value = []
  datosCliente.value = {
    nombre: '',
    telefono: '',
    email: '',
    aceptaCondiciones: false
  }
  turnoCreado.value = null
  pasoActual.value = 1
  // Scroll al inicio
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function irCrearCuenta() {
  // TODO: Redirigir a página de creación de cuenta con datos pre-cargados
  alert('Funcionalidad de creación de cuenta en desarrollo. ¡Pronto disponible!')
  cerrarModal()
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
  const fecha = new Date(fechaISO + 'T00:00:00')
  const dia = fecha.getDate()
  const mes = mesesNombre[fecha.getMonth()]
  const año = fecha.getFullYear()
  const diaSemana = diasSemana[fecha.getDay()]
  return `${diaSemana} ${dia} de ${mes} ${año}`
}

function formatearHora(isoString: string): string {
  const fecha = new Date(isoString)
  return fecha.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.reservar-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.empresa-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.empresa-header h1 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.empresa-info {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 10px;
  color: #666;
}

.stepper {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40px;
  position: relative;
}

.stepper::before {
  content: '';
  position: absolute;
  top: 20px;
  left: 10%;
  right: 10%;
  height: 2px;
  background: #e0e0e0;
  z-index: 0;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  transition: all 0.3s;
}

.step.active .step-number {
  background: #007bff;
  color: white;
}

.step.completed .step-number {
  background: #28a745;
  color: white;
}

.step-label {
  font-size: 14px;
  color: #666;
}

.paso-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.paso-content h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #2c3e50;
}

.btn-back {
  margin-bottom: 20px;
}

.loading, .empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

/* Servicios Grid */
.servicios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.servicio-card {
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.servicio-card:hover {
  border-color: #007bff;
  box-shadow: 0 4px 12px rgba(0,123,255,0.2);
  transform: translateY(-2px);
}

.servicio-card h3 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.servicio-card .descripcion {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.servicio-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.duracion {
  color: #666;
}

.precio {
  font-size: 20px;
  color: #28a745;
}

/* Profesionales List */
.profesionales-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.profesional-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.profesional-card:hover {
  border-color: #007bff;
  box-shadow: 0 4px 12px rgba(0,123,255,0.2);
}

.profesional-info h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
}

.profesional-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.arrow {
  font-size: 24px;
  color: #007bff;
}

/* Calendario */
.calendario-container {
  max-width: 600px;
  margin: 0 auto;
}

.calendario-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.calendario-header button {
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 18px;
}

.calendario-header button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.calendario-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.dia-header {
  text-align: center;
  font-weight: bold;
  padding: 10px;
  color: #666;
}

.dia-cell {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
}

.dia-cell:not(.disabled):hover {
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

/* Slots List */
.fecha-seleccionada {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 20px;
}

.slots-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 15px;
}

.slot-btn {
  padding: 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
}

.slot-btn:hover {
  border-color: #007bff;
  background: #e7f3ff;
  transform: translateY(-2px);
}

/* Resumen */
.resumen-reserva {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.resumen-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
}

.resumen-item:last-child {
  border-bottom: none;
}

.formulario-cliente {
  margin-top: 30px;
}

.formulario-cliente h3 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 4px;
  font-size: 16px;
}

.form-group input:focus {
  outline: none;
  border-color: #007bff;
}

.info-text {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 4px;
  padding: 15px;
  margin: 20px 0;
  color: #856404;
}

.btn-primary, .btn-secondary {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.btn-outline {
  background: transparent;
  color: #6c757d;
  border: 2px solid #6c757d;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
}

.btn-outline:hover {
  background: #6c757d;
  color: white;
}

.modal-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
}

.btn-confirmar {
  width: 100%;
  padding: 15px;
  font-size: 18px;
}

/* Modal */
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
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h2 {
  margin-top: 0;
  color: #28a745;
}

.resumen-completo {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
}

.resumen-completo p {
  margin: 10px 0;
}

.resumen-completo hr {
  margin: 15px 0;
  border: none;
  border-top: 2px solid #e0e0e0;
}

.turno-numero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.turno-numero .label {
  font-size: 14px;
  font-weight: normal;
}

.turno-numero .numero {
  font-size: 24px;
  font-weight: bold;
}

.badge-estado {
  background: #28a745;
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.info-box {
  background: #e3f2fd;
  border-left: 4px solid #2196f3;
  padding: 15px;
  border-radius: 4px;
  margin: 20px 0;
}

.info-text {
  margin: 0;
  color: #1976d2;
  font-size: 14px;
}

.cta-cuenta {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  padding: 25px;
  border-radius: 12px;
  margin: 20px 0;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.cta-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

.cta-cuenta h3 {
  color: #2d3436;
  margin: 10px 0;
  font-size: 20px;
}

.cta-cuenta p {
  color: #636e72;
  margin: 10px 0;
  font-size: 16px;
}

.cta-cuenta ul {
  list-style: none;
  padding: 0;
  margin: 15px 0;
  text-align: left;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}

.cta-cuenta li {
  padding: 8px 0;
  color: #2d3436;
  font-size: 14px;
}

.btn-secondary {
  background: #6c5ce7;
  color: white;
  border: none;
  padding: 12px 30px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  margin-top: 15px;
  transition: all 0.3s ease;
}

.btn-secondary:hover {
  background: #5f4dd1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(108, 92, 231, 0.4);
}

.advertencia {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
  padding: 15px;
  border-radius: 4px;
  margin: 20px 0;
}
</style>
