<template>
  <div class="dueno-container">
    <!-- Header -->
    <header class="dueno-header">
      <h1>Mi Empresa</h1>
      <div class="user-info">
        <span>{{ authStore.usuario?.nombre }} {{ authStore.usuario?.apellido }}</span>
        <span class="badge-dueno">Dueño</span>
        <button @click="handleLogout" class="btn-logout">Cerrar Sesión</button>
      </div>
    </header>

    <!-- Tabs -->
    <div class="tabs">
      <button 
        :class="['tab', { active: activeTab === 'profesionales' }]" 
        @click="activeTab = 'profesionales'"
      >
        Profesionales
      </button>
      <button 
        :class="['tab', { active: activeTab === 'servicios' }]" 
        @click="activeTab = 'servicios'"
      >
        Servicios
      </button>
      <button 
        :class="['tab', { active: activeTab === 'horarios' }]" 
        @click="activeTab = 'horarios'"
      >
        Horarios de la Empresa
      </button>
    </div>

    <!-- Tab: Profesionales -->
    <main v-if="activeTab === 'profesionales'" class="dueno-content">
      <div class="section-header">
        <h2>Profesionales de la Empresa</h2>
        <button @click="openModal()" class="btn-add">+ Agregar Profesional</button>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="loading">Cargando...</div>

      <!-- Profesionales Grid -->
      <div v-else-if="profesionales.length > 0" class="profesionales-grid">
        <div v-for="profesional in profesionales" :key="profesional.id" class="profesional-card">
          <div class="card-header">
            <h3>{{ profesional.nombre }} {{ profesional.apellido }}</h3>
            <span :class="['badge-status', profesional.activo ? 'activo' : 'inactivo']">
              {{ profesional.activo ? 'Activo' : 'Inactivo' }}
            </span>
          </div>
          <div class="card-body">
            <div class="info-item">
              <strong>Email:</strong> {{ profesional.email }}
            </div>
            <div class="info-item">
              <strong>Teléfono:</strong> {{ profesional.telefono || 'No especificado' }}
            </div>
            <div class="info-item">
              <strong>Especialidades:</strong> 
              <span class="especialidades-tags">
                <span v-for="(esp, index) in profesional.especialidades" :key="index" class="tag-especialidad">
                  {{ esp }}
                </span>
              </span>
            </div>
            <div class="info-item" v-if="profesional.descripcion">
              <strong>Descripción:</strong> {{ profesional.descripcion }}
            </div>
          </div>
          <div class="card-actions">
            <button @click="openModal(profesional)" class="btn-edit">Editar</button>
            <button 
              @click="toggleEstadoProfesional(profesional)" 
              :class="profesional.activo ? 'btn-delete' : 'btn-activate'"
            >
              {{ profesional.activo ? 'Desactivar' : 'Activar' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="empty-state">
        <p>No hay profesionales registrados</p>
        <p class="empty-hint">Haz clic en "Agregar Profesional" para comenzar</p>
      </div>
    </main>

    <!-- Tab: Servicios -->
    <main v-if="activeTab === 'servicios'" class="dueno-content">
      <div class="section-header">
        <h2>Servicios de la Empresa</h2>
        <button @click="openModalServicio()" class="btn-add">+ Agregar Servicio</button>
      </div>

      <!-- Loading State -->
      <div v-if="loadingServicios" class="loading">Cargando...</div>

      <!-- Servicios Grid -->
      <div v-else-if="servicios.length > 0" class="profesionales-grid">
        <div v-for="servicio in servicios" :key="servicio.id" class="profesional-card">
          <div class="card-header">
            <h3>{{ servicio.nombre }}</h3>
            <span :class="['badge-status', servicio.activo ? 'activo' : 'inactivo']">
              {{ servicio.activo ? 'Activo' : 'Inactivo' }}
            </span>
          </div>
          <div class="card-body">
            <div class="info-item" v-if="servicio.descripcion">
              <strong>Descripción:</strong> {{ servicio.descripcion }}
            </div>
            <div class="info-item">
              <strong>Duración:</strong> {{ servicio.duracionMinutos }} minutos
            </div>
            <div class="info-item">
              <strong>Precio:</strong> ${{ servicio.precio }}
            </div>
            <div class="info-item">
              <strong>Especialidades:</strong> 
              <span class="especialidades-tags">
                <span v-for="(esp, index) in servicio.especialidades" :key="index" class="tag-especialidad">
                  {{ esp }}
                </span>
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button @click="openModalServicio(servicio)" class="btn-edit">Editar</button>
            <button 
              @click="toggleServicioActivo(servicio)" 
              :class="servicio.activo ? 'btn-delete' : 'btn-activate'"
            >
              {{ servicio.activo ? 'Desactivar' : 'Activar' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="empty-state">
        <p>No hay servicios registrados</p>
        <p class="empty-hint">Haz clic en "Agregar Servicio" para comenzar</p>
      </div>
    </main>

    <!-- Tab: Horarios de la Empresa -->
    <main v-if="activeTab === 'horarios'" class="dueno-content">
      <div class="section-header">
        <h2>Horarios de Atención</h2>
        <button @click="openModalHorario()" class="btn-add">+ Agregar Horario</button>
      </div>

      <!-- Loading State -->
      <div v-if="loadingHorarios" class="loading">Cargando...</div>

      <!-- Horarios por Día -->
      <div v-else-if="horariosAgrupados && Object.keys(horariosAgrupados).length > 0" class="horarios-container">
        <div v-for="(dia, index) in diasSemana" :key="dia" class="horario-dia-card">
          <div class="dia-header">
            <div class="dia-header-left">
              <h3>{{ nombresDias[dia] }}</h3>
              <span v-if="horariosAgrupados[dia] && horariosAgrupados[dia].length > 0" class="count-badge">
                {{ horariosAgrupados[dia].length }} {{ horariosAgrupados[dia].length === 1 ? 'horario' : 'horarios' }}
              </span>
            </div>
            <button 
              v-if="horariosAgrupados[dia] && horariosAgrupados[dia].length > 0"
              @click="abrirModalCopiar(dia)" 
              class="btn-copiar-horarios"
              title="Copiar a otros días">
              📋 Copiar
            </button>
          </div>
          <div class="dia-body">
            <div v-if="horariosAgrupados[dia] && horariosAgrupados[dia].length > 0" class="horarios-list">
              <div v-for="horario in horariosAgrupados[dia]" :key="horario.id" class="horario-item">
                <div class="horario-info">
                  <span class="horario-time">{{ horario.horaInicio }} - {{ horario.horaFin }}</span>
                </div>
                <div class="horario-actions">
                  <button @click="openModalHorario(horario)" class="btn-edit-small">✏️</button>
                  <button @click="confirmarEliminarHorario(horario)" class="btn-delete-small">🗑️</button>
                </div>
              </div>
            </div>
            <div v-else class="no-horarios">
              <span>Sin horarios configurados</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-else class="empty-state">
        <p>No hay horarios configurados</p>
        <p class="empty-hint">Configura los horarios de atención de tu empresa por día de la semana</p>
      </div>
    </main>

    <!-- Modal Form Profesionales -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>{{ editingProfesional ? 'Editar' : 'Nuevo' }} Profesional</h2>
          <button @click="closeModal" class="btn-close">&times;</button>
        </div>
        <form @submit.prevent="submitForm" class="modal-form">
          <div class="form-row">
            <div class="form-group" :class="{ 'has-error': fieldErrors.nombre }">
              <label>Nombre *</label>
              <input 
                v-model="formData.nombre" 
                type="text" 
                required
                placeholder="Nombre"
              />
              <span v-if="fieldErrors.nombre" class="field-error">{{ fieldErrors.nombre }}</span>
            </div>
            <div class="form-group" :class="{ 'has-error': fieldErrors.apellido }">
              <label>Apellido *</label>
              <input 
                v-model="formData.apellido" 
                type="text" 
                required
                placeholder="Apellido"
              />
              <span v-if="fieldErrors.apellido" class="field-error">{{ fieldErrors.apellido }}</span>
            </div>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrors.email }">
            <label>Email *</label>
            <input 
              v-model="formData.email" 
              type="email" 
              required
              placeholder="email@ejemplo.com"
            />
            <span v-if="fieldErrors.email" class="field-error">{{ fieldErrors.email }}</span>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrors.contrasena }">
            <label>{{ editingProfesional ? 'Nueva Contraseña (opcional)' : 'Contraseña *' }}</label>
            <input 
              v-model="formData.contrasena" 
              type="password" 
              :required="!editingProfesional"
              placeholder="Contraseña (mínimo 8 caracteres)"
            />
            <span v-if="fieldErrors.contrasena" class="field-error">{{ fieldErrors.contrasena }}</span>
          </div>

          <div class="form-row">
            <div class="form-group" :class="{ 'has-error': fieldErrors.telefono }">
              <label>Teléfono</label>
              <input 
                v-model="formData.telefono" 
                type="tel" 
                placeholder="Solo números (10-15 dígitos)"
              />
              <span v-if="fieldErrors.telefono" class="field-error">{{ fieldErrors.telefono }}</span>
            </div>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrors.especialidades }">
            <label>Especialidades *</label>
            <div v-if="especialidadesDisponibles.length > 0" class="checkbox-group">
              <label v-for="esp in especialidadesDisponibles" :key="esp.id" class="checkbox-item">
                <input 
                  type="checkbox" 
                  :value="esp.nombre"
                  v-model="formData.especialidades"
                />
                <span>{{ esp.nombre }}</span>
              </label>
            </div>
            <div v-else class="loading-text">Cargando especialidades...</div>
            <small>Seleccione al menos una especialidad</small>
            <span v-if="fieldErrors.especialidades" class="field-error">{{ fieldErrors.especialidades }}</span>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrors.descripcion }">
            <label>Descripción</label>
            <textarea 
              v-model="formData.descripcion" 
              rows="3"
              placeholder="Descripción adicional del profesional"
            ></textarea>
            <span v-if="fieldErrors.descripcion" class="field-error">{{ fieldErrors.descripcion }}</span>
          </div>

          <!-- Gestión de Servicios (solo al editar) -->
          <div v-if="editingProfesional" class="form-group">
            <label>Servicios Disponibles</label>
            <button 
              type="button" 
              @click="cargarServiciosProfesional" 
              class="btn-secondary"
              :disabled="cargandoServicios"
            >
              {{ cargandoServicios ? 'Cargando...' : 'Gestionar Servicios' }}
            </button>
            
            <div v-if="serviciosProfesional.length > 0" class="servicios-profesional-list">
              <div 
                v-for="servicio in serviciosProfesional" 
                :key="servicio.servicioId" 
                class="servicio-item"
                :class="{ 'desactivado': !servicio.disponible }"
              >
                <div class="servicio-info">
                  <strong>{{ servicio.nombre }}</strong>
                  <span class="servicio-meta">{{ servicio.duracionMinutos }} min - ${{ servicio.precio }}</span>
                  <span v-if="servicio.heredado" class="badge-heredado">Por especialidad</span>
                  <span v-else class="badge-override">Modificado</span>
                </div>
                <button 
                  type="button"
                  @click="toggleServicio(servicio)"
                  :class="['btn-toggle', servicio.disponible ? 'activo' : 'inactivo']"
                  :disabled="submittingToggle"
                >
                  {{ servicio.disponible ? 'Desactivar' : 'Activar' }}
                </button>
              </div>
            </div>
          </div>

          <div v-if="error" class="error-message">{{ error }}</div>

          <div class="modal-actions">
            <button type="button" @click="closeModal" class="btn-cancel">Cancelar</button>
            <button type="submit" class="btn-submit" :disabled="submitting">
              {{ submitting ? 'Guardando...' : (editingProfesional ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Form Servicios -->
    <div v-if="showModalServicio" class="modal-overlay" @click="closeModalServicio">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h2>{{ editingServicio ? 'Editar' : 'Nuevo' }} Servicio</h2>
          <button @click="closeModalServicio" class="btn-close">&times;</button>
        </div>
        <form @submit.prevent="submitFormServicio" class="modal-form">
          <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.nombre }">
            <label>Nombre del Servicio *</label>
            <input 
              v-model="formDataServicio.nombre" 
              type="text" 
              required
              placeholder="Ej: Corte de cabello"
            />
            <span v-if="fieldErrorsServicio.nombre" class="field-error">{{ fieldErrorsServicio.nombre }}</span>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.descripcion }">
            <label>Descripción</label>
            <textarea 
              v-model="formDataServicio.descripcion" 
              rows="3"
              placeholder="Descripción del servicio"
            ></textarea>
            <span v-if="fieldErrorsServicio.descripcion" class="field-error">{{ fieldErrorsServicio.descripcion }}</span>
          </div>

          <div class="form-row">
            <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.duracionMinutos }">
              <label>Duración (minutos) *</label>
              <input 
                v-model.number="formDataServicio.duracionMinutos" 
                type="number" 
                required
                min="1"
                placeholder="45"
              />
              <span v-if="fieldErrorsServicio.duracionMinutos" class="field-error">{{ fieldErrorsServicio.duracionMinutos }}</span>
            </div>

            <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.precio }">
              <label>Precio ($) *</label>
              <input 
                v-model.number="formDataServicio.precio" 
                type="number" 
                required
                min="0"
                step="0.01"
                placeholder="5000"
              />
              <span v-if="fieldErrorsServicio.precio" class="field-error">{{ fieldErrorsServicio.precio }}</span>
            </div>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.bufferMinutos }">
            <label>Buffer (minutos)</label>
            <input 
              v-model.number="formDataServicio.bufferMinutos" 
              type="number" 
              min="0"
              placeholder="Tiempo de preparación después del servicio (opcional)"
            />
            <small class="field-hint">Si no se especifica, se usará el buffer del profesional o de la empresa</small>
            <span v-if="fieldErrorsServicio.bufferMinutos" class="field-error">{{ fieldErrorsServicio.bufferMinutos }}</span>
          </div>

          <div class="form-group" :class="{ 'has-error': fieldErrorsServicio.especialidades }">
            <label>Especialidades *</label>
            <div v-if="especialidadesDisponibles.length > 0" class="checkbox-group">
              <label v-for="esp in especialidadesDisponibles" :key="esp.id" class="checkbox-item">
                <input 
                  type="checkbox" 
                  :value="esp.nombre"
                  v-model="formDataServicio.especialidades"
                />
                <span>{{ esp.nombre }}</span>
              </label>
            </div>
            <div v-else class="loading-text">Cargando especialidades...</div>
            <small>El servicio estará disponible para profesionales con estas especialidades</small>
            <span v-if="fieldErrorsServicio.especialidades" class="field-error">{{ fieldErrorsServicio.especialidades }}</span>
          </div>

          <div v-if="errorServicio" class="error-message">{{ errorServicio }}</div>

          <div class="modal-actions">
            <button type="button" @click="closeModalServicio" class="btn-cancel">Cancelar</button>
            <button type="submit" class="btn-submit" :disabled="submittingServicio">
              {{ submittingServicio ? 'Guardando...' : (editingServicio ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Form Horarios -->
    <div v-if="showModalHorario" class="modal-overlay" @click="closeModalHorario">
      <div class="modal modal-small" @click.stop>
        <div class="modal-header">
          <h2>{{ editingHorario ? 'Editar' : 'Nuevo' }} Horario</h2>
          <button @click="closeModalHorario" class="btn-close">&times;</button>
        </div>
        <form @submit.prevent="submitFormHorario" class="modal-form">
          <div class="form-group" :class="{ 'has-error': fieldErrorsHorario.diaSemana }">
            <label>Día de la Semana *</label>
            <select v-model="formDataHorario.diaSemana" required>
              <option value="">Seleccione un día</option>
              <option v-for="dia in diasSemana" :key="dia" :value="dia">
                {{ nombresDias[dia] }}
              </option>
            </select>
            <span v-if="fieldErrorsHorario.diaSemana" class="field-error">{{ fieldErrorsHorario.diaSemana }}</span>
          </div>

          <div class="form-row">
            <div class="form-group" :class="{ 'has-error': fieldErrorsHorario.horaInicio }">
              <label>Hora Inicio *</label>
              <input 
                v-model="formDataHorario.horaInicio" 
                type="time" 
                required
              />
              <span v-if="fieldErrorsHorario.horaInicio" class="field-error">{{ fieldErrorsHorario.horaInicio }}</span>
            </div>
            <div class="form-group" :class="{ 'has-error': fieldErrorsHorario.horaFin }">
              <label>Hora Fin *</label>
              <input 
                v-model="formDataHorario.horaFin" 
                type="time" 
                required
              />
              <span v-if="fieldErrorsHorario.horaFin" class="field-error">{{ fieldErrorsHorario.horaFin }}</span>
            </div>
          </div>

          <div v-if="errorHorario" class="error-message">{{ errorHorario }}</div>

          <div class="modal-actions">
            <button type="button" @click="closeModalHorario" class="btn-cancel">Cancelar</button>
            <button type="submit" class="btn-submit" :disabled="submittingHorario">
              {{ submittingHorario ? 'Guardando...' : (editingHorario ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Copiar Horarios -->
    <div v-if="showModalCopiar" class="modal-overlay" @click="cerrarModalCopiar">
      <div class="modal modal-small" @click.stop>
        <div class="modal-header">
          <h2>Copiar Horarios de {{ nombresDias[diaFuenteCopia] }}</h2>
          <button @click="cerrarModalCopiar" class="btn-close">&times;</button>
        </div>
        <div class="modal-form">
          <div class="form-group">
            <label>Seleccione los días destino:</label>
            <div class="checkbox-group">
              <label v-for="dia in diasSemana" :key="dia" class="checkbox-label">
                <input 
                  type="checkbox" 
                  :value="dia"
                  v-model="diasDestinoSeleccionados"
                  :disabled="dia === diaFuenteCopia"
                />
                <span :class="{ 'disabled-day': dia === diaFuenteCopia }">
                  {{ nombresDias[dia] }}
                  <span v-if="dia === diaFuenteCopia" class="badge-fuente">(fuente)</span>
                </span>
              </label>
            </div>
          </div>

          <div v-if="diasConConflicto.length > 0" class="warning-message">
            ⚠️ Los siguientes días ya tienen horarios configurados y serán reemplazados:
            <strong>{{ diasConConflicto.map(d => nombresDias[d]).join(', ') }}</strong>
          </div>

          <div v-if="errorCopiar" class="error-message">{{ errorCopiar }}</div>

          <div class="modal-actions">
            <button type="button" @click="cerrarModalCopiar" class="btn-cancel">Cancelar</button>
            <button 
              type="button" 
              @click="confirmarCopiarHorarios" 
              class="btn-submit" 
              :disabled="submittingCopiar || diasDestinoSeleccionados.length === 0">
              {{ submittingCopiar ? 'Copiando...' : 'Copiar Horarios' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../services/api'
import { servicioService, type ServicioRequest, type ServicioResponse } from '../services/servicios'
import { especialidadService, type EspecialidadResponse } from '../services/especialidades'

const router = useRouter()
const authStore = useAuthStore()

// Tab activo
const activeTab = ref<'profesionales' | 'servicios' | 'horarios'>('profesionales')

// Estado para Profesionales
const profesionales = ref<any[]>([])
const especialidadesDisponibles = ref<EspecialidadResponse[]>([])
const serviciosProfesional = ref<any[]>([])
const cargandoServicios = ref(false)
const submittingToggle = ref(false)
const loading = ref(false)
const showModal = ref(false)
const editingProfesional = ref<any>(null)
const error = ref('')
const submitting = ref(false)
const fieldErrors = ref<Record<string, string>>({})

const formData = ref({
  nombre: '',
  apellido: '',
  email: '',
  contrasena: '',
  telefono: '',
  especialidades: [] as string[],
  descripcion: ''
})

// Estado para Servicios
const servicios = ref<ServicioResponse[]>([])
const loadingServicios = ref(false)
const showModalServicio = ref(false)
const editingServicio = ref<ServicioResponse | null>(null)
const errorServicio = ref('')
const submittingServicio = ref(false)
const fieldErrorsServicio = ref<Record<string, string>>({})

const formDataServicio = ref<ServicioRequest>({
  nombre: '',
  descripcion: '',
  duracionMinutos: 0,
  bufferMinutos: null,
  precio: 0,
  especialidades: []
})

// Estado para Horarios
const horarios = ref<any[]>([])
const loadingHorarios = ref(false)
const showModalHorario = ref(false)
const editingHorario = ref<any>(null)
const errorHorario = ref('')
const submittingHorario = ref(false)
const fieldErrorsHorario = ref<Record<string, string>>({})

const formDataHorario = ref({
  diaSemana: '',
  horaInicio: '',
  horaFin: ''
})

const diasSemana = ['LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO']
const nombresDias: Record<string, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
  SABADO: 'Sábado',
  DOMINGO: 'Domingo'
}

// Agrupar horarios por día
const horariosAgrupados = ref<Record<string, any[]>>({})

// Estado para Copiar Horarios
const showModalCopiar = ref(false)
const diaFuenteCopia = ref<string>('')
const diasDestinoSeleccionados = ref<string[]>([])
const submittingCopiar = ref(false)
const errorCopiar = ref('')

// Computed para detectar conflictos
const diasConConflicto = computed(() => {
  return diasDestinoSeleccionados.value.filter(dia => {
    return horariosAgrupados.value[dia] && horariosAgrupados.value[dia].length > 0
  })
})

function agruparHorariosPorDia() {
  const agrupados: Record<string, any[]> = {}
  diasSemana.forEach(dia => {
    agrupados[dia] = horarios.value.filter(h => h.diaSemana === dia)
  })
  horariosAgrupados.value = agrupados
}

onMounted(() => {
  cargarProfesionales()
  cargarServicios()
  cargarHorarios()
  cargarEspecialidadesDisponibles()
})

async function cargarProfesionales() {
  loading.value = true
  try {
    const response = await api.get('/dueno/profesionales')
    profesionales.value = response.data.datos || response.data || []
  } catch (err: any) {
    console.error('Error al cargar profesionales:', err)
    error.value = 'Error al cargar la lista de profesionales'
  } finally {
    loading.value = false
  }
}

async function cargarEspecialidadesDisponibles() {
  try {
    especialidadesDisponibles.value = await especialidadService.obtenerEspecialidadesActivas()
  } catch (err: any) {
    console.error('Error al cargar especialidades:', err)
    error.value = 'Error al cargar las especialidades disponibles'
  }
}

function openModal(profesional: any = null) {
  editingProfesional.value = profesional
  serviciosProfesional.value = [] // Limpiar servicios al abrir modal
  if (profesional) {
    formData.value = {
      nombre: profesional.nombre,
      apellido: profesional.apellido,
      email: profesional.email,
      contrasena: '',
      telefono: profesional.telefono || '',
      especialidades: profesional.especialidades || [],
      descripcion: profesional.descripcion || ''
    }
  } else {
    formData.value = {
      nombre: '',
      apellido: '',
      email: '',
      contrasena: '',
      telefono: '',
      especialidades: [],
      descripcion: ''
    }
  }
  error.value = ''
  fieldErrors.value = {}
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingProfesional.value = null
  serviciosProfesional.value = []
  error.value = ''
  fieldErrors.value = {}
}

async function cargarServiciosProfesional() {
  if (!editingProfesional.value) return
  
  cargandoServicios.value = true
  try {
    const response = await api.get(`/dueno/profesionales/${editingProfesional.value.id}/servicios`)
    serviciosProfesional.value = response.data.datos
  } catch (err: any) {
    console.error('Error al cargar servicios:', err)
    alert('Error al cargar servicios del profesional')
  } finally {
    cargandoServicios.value = false
  }
}

async function toggleServicio(servicio: any) {
  if (!editingProfesional.value) return
  
  submittingToggle.value = true
  try {
    await api.patch(`/dueno/profesionales/${editingProfesional.value.id}/servicios/toggle`, {
      servicioId: servicio.servicioId,
      disponible: !servicio.disponible
    })
    
    // Actualizar localmente
    servicio.disponible = !servicio.disponible
    servicio.heredado = servicio.disponible // Si se activa, vuelve a ser heredado
  } catch (err: any) {
    console.error('Error al cambiar servicio:', err)
    alert(err.response?.data?.mensaje || 'Error al cambiar el estado del servicio')
  } finally {
    submittingToggle.value = false
  }
}

async function submitForm() {
  error.value = ''
  fieldErrors.value = {}
  submitting.value = true
  
  try {
    // Validar que se haya seleccionado al menos una especialidad
    if (formData.value.especialidades.length === 0) {
      fieldErrors.value.especialidades = 'Debe seleccionar al menos una especialidad'
      submitting.value = false
      return
    }

    const payload = {
      nombre: formData.value.nombre,
      apellido: formData.value.apellido,
      email: formData.value.email,
      contrasena: formData.value.contrasena,
      telefono: formData.value.telefono,
      especialidades: formData.value.especialidades,
      descripcion: formData.value.descripcion
      // empresaId ya no es necesario - el backend lo obtiene automáticamente del dueño autenticado
    }

    if (editingProfesional.value) {
      // Actualizar profesional existente
      await api.put(`/dueno/profesionales/${editingProfesional.value.id}`, payload)
    } else {
      // Crear nuevo profesional
      await api.post('/dueno/profesionales', payload)
    }
    closeModal()
    await cargarProfesionales()
  } catch (err: any) {
    console.error('Error completo:', err)
    console.error('Response data:', err.response?.data)
    console.error('Response status:', err.response?.status)
    
    // Extraer y asignar errores por campo
    if (err.response?.data?.errores) {
      // Formato ErrorValidacion con errores por campo
      fieldErrors.value = err.response.data.errores
      error.value = 'Por favor corrija los errores en el formulario'
    } else if (err.response?.data?.mensaje) {
      // Formato RespuestaApi con mensaje general
      error.value = err.response.data.mensaje
    } else if (err.response?.data?.error) {
      error.value = err.response.data.error
    } else if (typeof err.response?.data === 'string') {
      error.value = err.response.data
    } else {
      error.value = 'Error al guardar el profesional. Por favor intente nuevamente.'
    }
  } finally {
    submitting.value = false
  }
}

async function toggleEstadoProfesional(profesional: any) {
  const accion = profesional.activo ? 'desactivar' : 'activar'
  const mensaje = profesional.activo 
    ? `¿Está seguro de desactivar al profesional ${profesional.nombre} ${profesional.apellido}?`
    : `¿Está seguro de activar al profesional ${profesional.nombre} ${profesional.apellido}?`
  
  if (confirm(mensaje)) {
    try {
      if (profesional.activo) {
        await api.patch(`/dueno/profesionales/${profesional.id}/desactivar`)
        alert('Profesional desactivado correctamente')
      } else {
        await api.patch(`/dueno/profesionales/${profesional.id}/activar`)
        alert('Profesional activado correctamente')
      }
      await cargarProfesionales()
    } catch (err) {
      console.error(`Error al ${accion} profesional:`, err)
      alert(`Error al ${accion} el profesional`)
    }
  }
}

function handleLogout() {
  // Llamar al endpoint de logout para invalidar sesión del servidor
  api.logout()
    .then(() => {
      authStore.logout()
      router.push('/login')
    })
    .catch(() => {
      // Incluso si falla, limpiar estado local
      authStore.logout()
      router.push('/login')
    })
}

// ==================== FUNCIONES PARA SERVICIOS ====================

async function cargarServicios() {
  loadingServicios.value = true
  try {
    servicios.value = await servicioService.obtenerServicios()
  } catch (err: any) {
    console.error('Error al cargar servicios:', err)
    errorServicio.value = 'Error al cargar la lista de servicios'
  } finally {
    loadingServicios.value = false
  }
}

function openModalServicio(servicio: ServicioResponse | null = null) {
  editingServicio.value = servicio
  if (servicio) {
    formDataServicio.value = {
      nombre: servicio.nombre,
      descripcion: servicio.descripcion,
      duracionMinutos: servicio.duracionMinutos,
      precio: servicio.precio,
      especialidades: [...servicio.especialidades]
    }
  } else {
    formDataServicio.value = {
      nombre: '',
      descripcion: '',
      duracionMinutos: 0,
      precio: 0,
      especialidades: []
    }
  }
  errorServicio.value = ''
  fieldErrorsServicio.value = {}
  showModalServicio.value = true
}

function closeModalServicio() {
  showModalServicio.value = false
  editingServicio.value = null
  formDataServicio.value = {
    nombre: '',
    descripcion: '',
    duracionMinutos: 0,
    precio: 0,
    especialidades: []
  }
  errorServicio.value = ''
  fieldErrorsServicio.value = {}
}

async function submitFormServicio() {
  submittingServicio.value = true
  errorServicio.value = ''
  fieldErrorsServicio.value = {}
  
  try {
    // Validar que se haya seleccionado al menos una especialidad
    if (formDataServicio.value.especialidades.length === 0) {
      fieldErrorsServicio.value.especialidades = 'Debe seleccionar al menos una especialidad'
      submittingServicio.value = false
      return
    }
    
    if (editingServicio.value) {
      await servicioService.actualizarServicio(editingServicio.value.id, formDataServicio.value)
    } else {
      await servicioService.crearServicio(formDataServicio.value)
    }
    closeModalServicio()
    await cargarServicios()
  } catch (err: any) {
    console.error('Error completo:', err)
    
    if (err.response?.data?.errores) {
      fieldErrorsServicio.value = err.response.data.errores
      errorServicio.value = 'Por favor corrija los errores en el formulario'
    } else if (err.response?.data?.mensaje) {
      errorServicio.value = err.response.data.mensaje
    } else if (err.response?.data?.error) {
      errorServicio.value = err.response.data.error
    } else {
      errorServicio.value = 'Error al guardar el servicio. Por favor intente nuevamente.'
    }
  } finally {
    submittingServicio.value = false
  }
}

async function toggleServicioActivo(servicio: ServicioResponse) {
  try {
    if (servicio.activo) {
      await servicioService.desactivarServicio(servicio.id)
    } else {
      await servicioService.activarServicio(servicio.id)
    }
    await cargarServicios()
  } catch (err) {
    console.error('Error al cambiar estado del servicio:', err)
    alert('Error al cambiar el estado del servicio')
  }
}

// ==================== FUNCIONES PARA HORARIOS ====================

async function cargarHorarios() {
  loadingHorarios.value = true
  try {
    const response = await api.obtenerHorariosEmpresa()
    // Verificar si la respuesta tiene estructura ApiResponse
    horarios.value = response.data.datos || response.data || []
    agruparHorariosPorDia()
  } catch (err: any) {
    console.error('Error al cargar horarios:', err)
    errorHorario.value = 'Error al cargar los horarios de la empresa'
  } finally {
    loadingHorarios.value = false
  }
}

function openModalHorario(horario: any = null) {
  if (horario) {
    editingHorario.value = horario
    formDataHorario.value = {
      diaSemana: horario.diaSemana,
      horaInicio: horario.horaInicio,
      horaFin: horario.horaFin
    }
  } else {
    editingHorario.value = null
    formDataHorario.value = {
      diaSemana: '',
      horaInicio: '',
      horaFin: ''
    }
  }
  fieldErrorsHorario.value = {}
  errorHorario.value = ''
  showModalHorario.value = true
}

function closeModalHorario() {
  showModalHorario.value = false
  editingHorario.value = null
  formDataHorario.value = {
    diaSemana: '',
    horaInicio: '',
    horaFin: ''
  }
  fieldErrorsHorario.value = {}
  errorHorario.value = ''
}

async function submitFormHorario() {
  fieldErrorsHorario.value = {}
  errorHorario.value = ''
  submittingHorario.value = true

  try {
    if (editingHorario.value) {
      await api.actualizarHorarioEmpresa(editingHorario.value.id, formDataHorario.value)
    } else {
      await api.crearHorarioEmpresa(formDataHorario.value)
    }
    
    await cargarHorarios()
    closeModalHorario()
  } catch (err: any) {
    console.error('Error al guardar horario:', err)
    
    if (err.response?.status === 400) {
      const mensaje = err.response.data.mensaje || err.response.data.message
      if (mensaje) {
        errorHorario.value = mensaje
      } else {
        errorHorario.value = 'Error al validar los datos del horario'
      }
    } else {
      errorHorario.value = 'Error al guardar el horario'
    }
  } finally {
    submittingHorario.value = false
  }
}

async function confirmarEliminarHorario(horario: any) {
  const confirmacion = confirm(
    `¿Estás seguro de eliminar el horario de ${nombresDias[horario.diaSemana]} (${horario.horaInicio} - ${horario.horaFin})?`
  )
  
  if (confirmacion) {
    await eliminarHorario(horario.id)
  }
}

async function eliminarHorario(id: number) {
  try {
    await api.eliminarHorarioEmpresa(id)
    await cargarHorarios()
  } catch (err: any) {
    console.error('Error al eliminar horario:', err)
    alert('Error al eliminar el horario')
  }
}

// ==================== FUNCIONES PARA COPIAR HORARIOS ====================

function abrirModalCopiar(diaFuente: string) {
  diaFuenteCopia.value = diaFuente
  diasDestinoSeleccionados.value = []
  errorCopiar.value = ''
  showModalCopiar.value = true
}

function cerrarModalCopiar() {
  showModalCopiar.value = false
  diaFuenteCopia.value = ''
  diasDestinoSeleccionados.value = []
  errorCopiar.value = ''
}

async function confirmarCopiarHorarios() {
  if (diasDestinoSeleccionados.value.length === 0) {
    errorCopiar.value = 'Debe seleccionar al menos un día destino'
    return
  }

  // Si hay conflictos, pedir confirmación explícita
  if (diasConConflicto.value.length > 0) {
    const confirmacion = confirm(
      `Los días ${diasConConflicto.value.map(d => nombresDias[d]).join(', ')} ya tienen horarios configurados.\n\n¿Desea reemplazarlos con los horarios de ${nombresDias[diaFuenteCopia.value]}?`
    )
    if (!confirmacion) {
      return
    }
  }

  submittingCopiar.value = true
  errorCopiar.value = ''

  try {
    await api.copiarHorariosAOtrosDias(
      diaFuenteCopia.value,
      diasDestinoSeleccionados.value,
      diasConConflicto.value.length > 0 // reemplazar solo si hay conflictos confirmados
    )
    
    await cargarHorarios()
    cerrarModalCopiar()
  } catch (err: any) {
    console.error('Error al copiar horarios:', err)
    
    if (err.response?.status === 400) {
      const mensaje = err.response.data.mensaje || err.response.data.message
      errorCopiar.value = mensaje || 'Error al copiar los horarios'
    } else {
      errorCopiar.value = 'Error al copiar los horarios'
    }
  } finally {
    submittingCopiar.value = false
  }
}

</script>

<style scoped>
.dueno-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-bottom: 2rem;
}

.dueno-header {
  background: white;
  padding: 1.5rem 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dueno-header h1 {
  font-size: 1.5rem;
  color: #2d3748;
  margin: 0;
}

/* Tabs */
.tabs {
  background: white;
  display: flex;
  gap: 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.tab {
  flex: 1;
  padding: 1rem 2rem;
  border: none;
  background: transparent;
  color: #718096;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}

.tab:hover {
  background: #f7fafc;
  color: #667eea;
}

.tab.active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: #f7fafc;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.badge-dueno {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0.4rem 1rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
}

.btn-logout {
  background: #e53e3e;
  color: white;
  border: none;
  padding: 0.5rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.btn-logout:hover {
  background: #c53030;
  transform: translateY(-2px);
}

.dueno-content {
  max-width: 1400px;
  margin: 2rem auto;
  padding: 0 2rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.section-header h2 {
  color: white;
  font-size: 1.8rem;
  margin: 0;
}

.btn-add {
  background: white;
  color: #667eea;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.15);
}

.loading {
  text-align: center;
  color: white;
  font-size: 1.2rem;
  padding: 3rem;
}

.profesionales-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
}

.profesional-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}

.profesional-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f7fafc;
}

.card-header h3 {
  color: #2d3748;
  font-size: 1.2rem;
  margin: 0;
}

.card-body {
  margin-bottom: 1rem;
}

.info-item {
  margin-bottom: 0.5rem;
  color: #4a5568;
  font-size: 0.95rem;
}

.info-item strong {
  color: #2d3748;
}

.especialidades-tags {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.tag-especialidad {
  background: #e6fffa;
  color: #234e52;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.btn-edit,
.btn-delete {
  flex: 1;
  padding: 0.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.875rem;
  transition: all 0.3s ease;
}

.btn-edit {
  background: #4299e1;
  color: white;
}

.btn-edit:hover {
  background: #3182ce;
}

.btn-delete {
  background: #f56565;
  color: white;
}

.btn-delete:hover {
  background: #e53e3e;
}

.btn-activate {
  background: #48bb78;
  color: white;
}

.btn-activate:hover {
  background: #38a169;
}

.empty-state {
  background: white;
  border-radius: 12px;
  padding: 3rem;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.empty-state p {
  color: #4a5568;
  font-size: 1.2rem;
  margin: 0.5rem 0;
}

.empty-hint {
  color: #718096;
  font-size: 1rem;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 2px solid #f7fafc;
}

.modal-header h2 {
  color: #2d3748;
  font-size: 1.5rem;
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 2rem;
  color: #a0aec0;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-close:hover {
  color: #2d3748;
}

.modal-form {
  padding: 1.5rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  color: #2d3748;
  font-weight: 600;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s ease;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  font-family: inherit;
  transition: border-color 0.3s ease;
  resize: vertical;
}

.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-group small {
  display: block;
  color: #718096;
  font-size: 0.75rem;
  margin-top: 0.25rem;
}

/* Checkbox Group Styles */
.checkbox-group {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #fff;
}

.checkbox-item:hover {
  border-color: #4299e1;
  background-color: #ebf8ff;
}

.checkbox-item input[type="checkbox"] {
  cursor: pointer;
  width: 18px;
  height: 18px;
  margin: 0;
}

.checkbox-item span {
  font-size: 0.9rem;
  color: #2d3748;
  user-select: none;
}

.loading-text {
  color: #718096;
  font-style: italic;
  padding: 0.5rem;
}

/* Field Error Styles */
.form-group.has-error input,
.form-group.has-error textarea {
  border-color: #fc8181;
  background-color: #fff5f5;
}

.form-group.has-error input:focus,
.form-group.has-error textarea:focus {
  border-color: #f56565;
  box-shadow: 0 0 0 3px rgba(245, 101, 101, 0.1);
}

.field-error {
  display: block;
  color: #e53e3e;
  font-size: 0.8rem;
  margin-top: 0.35rem;
  font-weight: 500;
}

.error-message {
  background: #fed7d7;
  color: #c53030;
  padding: 0.75rem;
  border-radius: 8px;
  margin-bottom: 1rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
}

.btn-cancel {
  background: #e2e8f0;
  color: #2d3748;
}

.btn-cancel:hover {
  background: #cbd5e0;
}

.btn-submit {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Servicios del Profesional */
.btn-secondary {
  padding: 0.6rem 1rem;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-secondary:hover:not(:disabled) {
  background: #3182ce;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.servicios-profesional-list {
  margin-top: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.servicio-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #e2e8f0;
  transition: background 0.2s;
}

.servicio-item:last-child {
  border-bottom: none;
}

.servicio-item:hover {
  background: #f7fafc;
}

.servicio-item.desactivado {
  opacity: 0.6;
  background: #fff5f5;
}

.servicio-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.servicio-info strong {
  color: #2d3748;
  font-size: 0.95rem;
}

.servicio-meta {
  color: #718096;
  font-size: 0.85rem;
}

.badge-heredado {
  display: inline-block;
  padding: 0.15rem 0.5rem;
  background: #bee3f8;
  color: #2c5282;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-top: 0.25rem;
}

.badge-override {
  display: inline-block;
  padding: 0.15rem 0.5rem;
  background: #feebc8;
  color: #c05621;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-top: 0.25rem;
}

.btn-toggle {
  padding: 0.4rem 0.8rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-toggle.activo {
  background: #fc8181;
  color: white;
}

.btn-toggle.activo:hover:not(:disabled) {
  background: #f56565;
}

.btn-toggle.inactivo {
  background: #48bb78;
  color: white;
}

.btn-toggle.inactivo:hover:not(:disabled) {
  background: #38a169;
}

.btn-toggle:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Estilos adicionales para servicios */
.badge-status {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.badge-status.activo {
  background: #c6f6d5;
  color: #22543d;
}

.badge-status.inactivo {
  background: #fed7d7;
  color: #c53030;
}

.btn-activate {
  background: #48bb78;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-activate:hover {
  background: #38a169;
  transform: translateY(-2px);
}

.especialidades-input {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.especialidades-input input {
  flex: 1;
}

.btn-add-esp {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-add-esp:hover {
  background: #5a67d8;
}

.especialidades-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.tag-especialidad-editable {
  background: #e6fffa;
  color: #234e52;
  padding: 0.4rem 0.75rem;
  border-radius: 16px;
  font-size: 0.875rem;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-remove-esp {
  background: transparent;
  border: none;
  color: #c53030;
  cursor: pointer;
  font-size: 1.25rem;
  line-height: 1;
  padding: 0;
  margin-left: 0.25rem;
}

.btn-remove-esp:hover {
  color: #9b2c2c;
}

@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .profesionales-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .tabs {
    flex-direction: column;
  }
  
  .tab {
    border-bottom: 1px solid #e2e8f0;
    border-left: 3px solid transparent;
  }
  
  .tab.active {
    border-left-color: #667eea;
    border-bottom-color: transparent;
  }
}

/* ==================== ESTILOS PARA HORARIOS ==================== */

.horarios-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-top: 2rem;
}

.horario-dia-card {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.dia-header {
  background-color: #667eea;
  color: white;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dia-header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.dia-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.btn-copiar-horarios {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.375rem 0.75rem;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-copiar-horarios:hover {
  background-color: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
}

.count-badge {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
}

.dia-body {
  padding: 1rem;
}

.horarios-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.horario-item {
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 0.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s ease;
}

.horario-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 4px rgba(102, 126, 234, 0.1);
}

.horario-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.horario-time {
  font-weight: 600;
  color: #1f2937;
  font-size: 1rem;
}

.horario-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-edit-small,
.btn-delete-small {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-edit-small {
  background-color: #f3f4f6;
  color: #4b5563;
}

.btn-edit-small:hover {
  background-color: #e5e7eb;
  color: #1f2937;
}

.btn-delete-small {
  background-color: #fee2e2;
  color: #dc2626;
}

.btn-delete-small:hover {
  background-color: #fecaca;
  color: #991b1b;
}

.no-horarios {
  color: #6b7280;
  font-size: 0.875rem;
  text-align: center;
  padding: 1rem;
  background-color: white;
  border: 1px dashed #d1d5db;
  border-radius: 6px;
}

.modal-small {
  max-width: 500px;
}

.modal-small .modal-content {
  max-width: 500px;
}

/* Estilos para Modal de Copiar */
.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.checkbox-label:hover {
  background-color: #f3f4f6;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"]:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.disabled-day {
  color: #9ca3af;
}

.badge-fuente {
  font-size: 0.75rem;
  color: #6b7280;
  font-style: italic;
}

.warning-message {
  background-color: #fef3c7;
  border: 1px solid #fbbf24;
  color: #92400e;
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  margin-top: 1rem;
}

.warning-message strong {
  font-weight: 600;
}
</style>

