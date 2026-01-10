<template>
  <div class="profesional-view">
    <!-- Header -->
    <header class="header">
      <div class="header-content">
        <h1>Panel de Profesional</h1>
        <button @click="cerrarSesion" class="btn-logout">Cerrar Sesión</button>
      </div>
    </header>

    <!-- Navigation Tabs -->
    <nav class="tabs-navigation">
      <button 
        @click="seccionActiva = 'disponibilidad'" 
        :class="['tab-btn', { active: seccionActiva === 'disponibilidad' }]">
        📅 Disponibilidad
      </button>
      <button 
        @click="seccionActiva = 'turnos'" 
        :class="['tab-btn', { active: seccionActiva === 'turnos' }]">
        📋 Mis Turnos
      </button>
      <button 
        @click="seccionActiva = 'bloqueos'" 
        :class="['tab-btn', { active: seccionActiva === 'bloqueos' }]">
        🚫 Bloqueos
      </button>
    </nav>

    <main class="main-content">
      <!-- Sección: Disponibilidad -->
      <section v-show="seccionActiva === 'disponibilidad'" class="section">
        <div class="section-header">
          <h2>📅 Mi Disponibilidad</h2>
          <button 
            v-if="disponibilidades.length > 0"
            @click="abrirModalDisponibilidad()" 
            class="btn-primary">
            + Agregar Horario
          </button>
        </div>

        <!-- Loading State -->
        <div v-if="loadingDisponibilidad" class="loading">Cargando disponibilidad...</div>

        <!-- Estado: Sin horarios propios (heredando de empresa) -->
        <div v-else-if="disponibilidades.length === 0" class="empty-state-disponibilidad">
          <div class="info-card">
            <div class="info-icon">ℹ️</div>
            <div class="info-content">
              <h3>No tenés horarios propios configurados</h3>
              <p>La disponibilidad se calcula usando los horarios de la empresa como fallback.</p>
            </div>
          </div>

          <button 
            @click="inicializarDesdeEmpresa" 
            class="btn-primary btn-large"
            :disabled="submittingInicializar">
            {{ submittingInicializar ? 'Inicializando...' : '🏢 Usar Horarios de la Empresa' }}
          </button>

          <div v-if="errorInicializar" class="error-message">{{ errorInicializar }}</div>

          <!-- Horarios de empresa como referencia -->
          <div v-if="horariosEmpresa.length > 0" class="horarios-referencia">
            <h3>📋 Horarios de la Empresa (referencia)</h3>
            <div class="horarios-referencia-grid">
              <div v-for="dia in diasSemana" :key="dia" class="dia-card-ref">
                <div class="dia-name">{{ nombresDias[dia] }}</div>
                <div v-if="horariosEmpresaAgrupados[dia] && horariosEmpresaAgrupados[dia].length > 0" class="horarios-ref-list">
                  <div v-for="horario in horariosEmpresaAgrupados[dia]" :key="horario.id" class="horario-ref-item">
                    {{ horario.horaInicio }} - {{ horario.horaFin }}
                  </div>
                </div>
                <div v-else class="sin-horarios-ref">Sin horarios</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Estado: Con horarios propios -->
        <div v-else class="disponibilidad-container">
          <!-- Disponibilidades agrupadas por día -->
          <div class="disponibilidad-grid">
            <div v-for="dia in diasSemana" :key="dia" class="dia-card">
              <div class="dia-header">
                <h3>{{ nombresDias[dia] }}</h3>
                <span v-if="disponibilidadesAgrupadas[dia] && disponibilidadesAgrupadas[dia].length > 0" class="count-badge">
                  {{ disponibilidadesAgrupadas[dia].length }}
                </span>
              </div>
              <div class="dia-body">
                <div v-if="disponibilidadesAgrupadas[dia] && disponibilidadesAgrupadas[dia].length > 0" class="horarios-list">
                  <div v-for="disp in disponibilidadesAgrupadas[dia]" :key="disp.id ?? `temp-${disp.diaSemana}-${disp.horaInicio}`" class="horario-item">
                    <span class="horario-time">{{ disp.horaInicio }} - {{ disp.horaFin }}</span>
                    <div class="horario-actions">
                      <button @click="abrirModalDisponibilidad(disp)" class="btn-edit-small">✏️</button>
                      <button @click="confirmarEliminarDisponibilidad(disp)" class="btn-delete-small">🗑️</button>
                    </div>
                  </div>
                </div>
                <div v-else class="sin-horarios">Sin horarios</div>
              </div>
            </div>
          </div>

          <!-- Panel colapsado: Ver horarios de empresa -->
          <details class="horarios-empresa-collapsible">
            <summary>📋 Ver Horarios de la Empresa (referencia)</summary>
            <div class="horarios-referencia-grid-small">
              <div v-for="dia in diasSemana" :key="dia" class="dia-ref-inline">
                <strong>{{ nombresDias[dia] }}:</strong>
                <span v-if="horariosEmpresaAgrupados[dia] && horariosEmpresaAgrupados[dia].length > 0">
                  {{ horariosEmpresaAgrupados[dia].map(h => `${h.horaInicio}-${h.horaFin}`).join(', ') }}
                </span>
                <span v-else class="text-muted">Sin horarios</span>
              </div>
            </div>
          </details>
        </div>
      </section>

      <!-- Sección: Mis Turnos -->
      <section v-show="seccionActiva === 'turnos'" class="section">
        <div class="section-header">
          <h2>📋 Mis Turnos</h2>
          <div class="filtros-turnos">
            <button 
              :class="['btn-filtro', { active: filtroTurnos === 'hoy' }]"
              @click="cambiarFiltroTurnos('hoy')">
              Hoy
            </button>
            <button 
              :class="['btn-filtro', { active: filtroTurnos === 'semana' }]"
              @click="cambiarFiltroTurnos('semana')">
              Esta Semana
            </button>
            <button 
              :class="['btn-filtro', { active: filtroTurnos === 'rango' }]"
              @click="cambiarFiltroTurnos('rango')">
              Rango
            </button>
          </div>
        </div>

        <!-- Filtro de rango -->
        <div v-if="filtroTurnos === 'rango'" class="rango-fechas">
          <input v-model="fechaDesde" type="date" />
          <span>a</span>
          <input v-model="fechaHasta" type="date" />
          <button @click="cargarTurnos" class="btn-primary-small">Buscar</button>
        </div>

        <!-- Loading State -->
        <div v-if="loadingTurnos" class="loading">Cargando turnos...</div>

        <!-- Lista de turnos -->
        <div v-else-if="turnos.length > 0" class="turnos-list">
          <div v-for="turno in turnosOrdenados" :key="turno.id" class="turno-card">
            <div class="turno-header">
              <div class="turno-fecha">
                <span class="fecha-dia">{{ formatearFechaLegible(turno.fecha) }}</span>
                <span class="turno-hora" :title="`Servicio: ${turno.duracionMinutos}min + Buffer: ${turno.bufferMinutos}min = Total: ${turno.duracionMinutos + turno.bufferMinutos}min`">
                  {{ turno.horaInicio }} - {{ turno.horaFin }}
                </span>
              </div>
              <span :class="['estado-badge', `estado-${turno.estado.toLowerCase()}`]">
                {{ turno.estado }}
              </span>
            </div>
            <div class="turno-body">
              <div class="turno-info-row">
                <strong>Servicio:</strong> {{ turno.servicioNombre }} ({{ turno.duracionMinutos }}min)
              </div>
              <div class="turno-info-row">
                <strong>Cliente:</strong> {{ turno.clienteNombre }}
              </div>
              <div class="turno-info-row">
                <strong>Teléfono:</strong> {{ turno.clienteTelefono }}
              </div>
              <div v-if="turno.clienteEmail" class="turno-info-row">
                <strong>Email:</strong> {{ turno.clienteEmail }}
              </div>
              <div class="turno-info-row">
                <strong>Duración:</strong> {{ turno.duracionMinutos }} min | <strong>Precio:</strong> ${{ turno.precio }}
              </div>
              <div v-if="turno.observaciones" class="turno-observaciones">
                <strong>Observaciones:</strong>
                <p>{{ turno.observaciones }}</p>
              </div>
            </div>
            <div class="turno-actions">
              <select 
                v-if="puedesCambiarEstado(turno)"
                @change="cambiarEstado(turno, ($event.target as HTMLSelectElement).value)"
                class="select-estado">
                <option value="">Cambiar estado</option>
                <option v-for="estado in estadosDisponibles(turno)" :key="estado.valor" :value="estado.valor">
                  {{ estado.label }}
                </option>
              </select>
              <button 
                @click="abrirModalObservaciones(turno)" 
                class="btn-secondary-small">
                + Observación
              </button>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="empty-state">
          <p>No hay turnos para el período seleccionado</p>
        </div>
      </section>

      <!-- Sección: Bloqueos -->
      <section v-show="seccionActiva === 'bloqueos'" class="section">
        <div class="section-header">
          <h2>🚫 Bloqueos de Fechas</h2>
          <button @click="abrirModalBloqueo()" class="btn-primary">+ Agregar Bloqueo</button>
        </div>

        <!-- Loading State -->
        <div v-if="loadingBloqueos" class="loading">Cargando bloqueos...</div>

        <!-- Lista de bloqueos -->
        <div v-else-if="bloqueos.length > 0" class="bloqueos-list">
          <div v-for="bloqueo in bloqueosOrdenados" :key="bloqueo.id" class="bloqueo-item">
            <div class="bloqueo-info">
              <div class="bloqueo-fechas">
                <span class="fecha-badge">{{ formatearFecha(bloqueo.fechaInicio) }}</span>
                <span v-if="bloqueo.fechaFin && bloqueo.fechaFin !== bloqueo.fechaInicio">
                  → <span class="fecha-badge">{{ formatearFecha(bloqueo.fechaFin) }}</span>
                </span>
              </div>
              <div v-if="bloqueo.motivo" class="bloqueo-motivo">{{ bloqueo.motivo }}</div>
            </div>
            <div class="bloqueo-actions">
              <button @click="abrirModalBloqueo(bloqueo)" class="btn-edit">✏️</button>
              <button @click="confirmarEliminarBloqueo(bloqueo)" class="btn-delete">🗑️</button>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="empty-state">
          <p>No hay bloqueos configurados</p>
          <p class="empty-hint">Los bloqueos te permiten marcar fechas donde no estarás disponible</p>
        </div>
      </section>
    </main>

    <!-- Modal: Observaciones Turno -->
    <div v-if="showModalObservaciones" class="modal-overlay" @click="cerrarModalObservaciones">
      <div class="modal modal-small" @click.stop>
        <div class="modal-header">
          <h2>Agregar Observación</h2>
          <button @click="cerrarModalObservaciones" class="btn-close">&times;</button>
        </div>
        <form @submit.prevent="submitObservaciones" class="modal-form">
          <div v-if="turnoSeleccionado" class="turno-info-modal">
            <p><strong>Cliente:</strong> {{ turnoSeleccionado.clienteNombre }}</p>
            <p><strong>Servicio:</strong> {{ turnoSeleccionado.servicioNombre }}</p>
            <p><strong>Fecha:</strong> {{ formatearFechaLegible(turnoSeleccionado.fecha) }} - {{ turnoSeleccionado.horaInicio }}</p>
          </div>

          <div class="form-group">
            <label>Observación *</label>
            <textarea 
              v-model="nuevaObservacion" 
              rows="4"
              placeholder="Escribe las observaciones del turno..."
              required
            ></textarea>
          </div>

          <div v-if="errorObservaciones" class="error-message">{{ errorObservaciones }}</div>

          <div class="modal-actions">
            <button type="button" @click="cerrarModalObservaciones" class="btn-cancel">Cancelar</button>
            <button type="submit" class="btn-submit" :disabled="submittingObservaciones">
              {{ submittingObservaciones ? 'Guardando...' : 'Agregar' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal: Disponibilidad -->
    <div v-if="showModalDisponibilidad" class="modal-overlay" @click="cerrarModalDisponibilidad">
      <div class="modal modal-small" @click.stop>
        <div class="modal-header">
          <h2>{{ editingDisponibilidad ? 'Editar' : 'Nuevo' }} Horario</h2>
          <button @click="cerrarModalDisponibilidad" class="btn-close">&times;</button>
        </div>
        <form @submit.prevent="submitFormDisponibilidad" class="modal-form">
          <div class="form-group">
            <label>Día de la Semana *</label>
            <select v-model="formDataDisponibilidad.diaSemana" required>
              <option value="">Seleccione un día</option>
              <option v-for="dia in diasSemana" :key="dia" :value="dia">
                {{ nombresDias[dia] }}
              </option>
            </select>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Hora Inicio *</label>
              <input 
                v-model="formDataDisponibilidad.horaInicio" 
                type="time" 
                required
              />
            </div>
            <div class="form-group">
              <label>Hora Fin *</label>
              <input 
                v-model="formDataDisponibilidad.horaFin" 
                type="time" 
                required
              />
            </div>
          </div>

          <div v-if="errorDisponibilidad" class="error-message">{{ errorDisponibilidad }}</div>

          <div class="modal-actions">
            <button type="button" @click="cerrarModalDisponibilidad" class="btn-cancel">Cancelar</button>
            <button type="submit" class="btn-submit" :disabled="submittingDisponibilidad">
              {{ submittingDisponibilidad ? 'Guardando...' : (editingDisponibilidad ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal: Bloqueo -->
    <div v-if="showModalBloqueo" class="modal-overlay" @click="cerrarModalBloqueo">
      <div class="modal modal-small" @click.stop>
        <div class="modal-header">
          <h2>{{ editingBloqueo ? 'Editar' : 'Nuevo' }} Bloqueo</h2>
          <button @click="cerrarModalBloqueo" class="btn-close">&times;</button>
        </div>
        <form @submit.prevent="submitFormBloqueo" class="modal-form">
          <div class="form-group">
            <label>Fecha Inicio *</label>
            <input 
              v-model="formDataBloqueo.fechaInicio" 
              type="date" 
              :min="fechaMinima"
              required
            />
          </div>

          <div class="form-group">
            <label>Fecha Fin (opcional)</label>
            <input 
              v-model="formDataBloqueo.fechaFin" 
              type="date" 
              :min="formDataBloqueo.fechaInicio || fechaMinima"
            />
            <small class="form-hint">Dejar vacío para bloquear solo la fecha de inicio</small>
          </div>

          <div class="form-group">
            <label>Motivo (opcional)</label>
            <textarea 
              v-model="formDataBloqueo.motivo" 
              rows="3"
              placeholder="Ej: Vacaciones, día personal, etc."
            ></textarea>
          </div>

          <div v-if="errorBloqueo" class="error-message">{{ errorBloqueo }}</div>

          <div class="modal-actions">
            <button type="button" @click="cerrarModalBloqueo" class="btn-cancel">Cancelar</button>
            <button type="submit" class="btn-submit" :disabled="submittingBloqueo">
              {{ submittingBloqueo ? 'Guardando...' : (editingBloqueo ? 'Actualizar' : 'Crear') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal: Conflictos con Turnos -->
    <div v-if="showModalConflictos" class="modal-overlay" @click="cerrarModalConflictos">
      <div class="modal modal-medium" @click.stop>
        <div class="modal-header">
          <h2>⚠️ Conflictos con Turnos Existentes</h2>
          <button @click="cerrarModalConflictos" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <p class="conflicto-mensaje">
            Se encontraron <strong>{{ conflictosData?.cantidadConflictos }}</strong> turno(s) 
            que están dentro del rango del bloqueo. Elegí cómo resolverlos:
          </p>

          <div class="turnos-conflictivos">
            <div v-for="turno in conflictosData?.turnosConflictivos" :key="turno.turnoId" class="turno-conflicto-item">
              <div class="turno-conflicto-info">
                <strong>{{ turno.fecha }} {{ turno.horaInicio }}-{{ turno.horaFin }}</strong>
                <p>{{ turno.servicioNombre }}</p>
                <p class="cliente-info">{{ turno.clienteNombre }} - {{ turno.clienteTelefono }}</p>
                <span :class="['estado-badge-small', `estado-${turno.estado.toLowerCase()}`]">
                  {{ turno.estado }}
                </span>
              </div>
            </div>
          </div>

          <div class="opciones-resolucion">
            <h3>Seleccioná una opción:</h3>
            
            <button @click="resolverConflicto('CANCELAR_TODOS')" class="opcion-btn opcion-cancelar">
              <span class="opcion-icono">❌</span>
              <div class="opcion-texto">
                <strong>Cancelar Todos los Turnos</strong>
                <small>Se cancelarán automáticamente todos los turnos en conflicto</small>
              </div>
            </button>

            <button @click="resolverConflicto('REPROGRAMAR')" class="opcion-btn opcion-reprogramar">
              <span class="opcion-icono">📅</span>
              <div class="opcion-texto">
                <strong>Reprogramar Manualmente</strong>
                <small>Elegir nueva fecha/hora para cada turno (paso a paso)</small>
              </div>
            </button>

            <button @click="resolverConflicto('CANCELAR_FUTUROS')" class="opcion-btn opcion-futuros">
              <span class="opcion-icono">⏩</span>
              <div class="opcion-texto">
                <strong>Cancelar Solo Futuros</strong>
                <small>Los turnos pasados se mantienen, solo se cancelan los futuros</small>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal: Reprogramación Paso a Paso -->
    <div v-if="showModalReprogramacion" class="modal-overlay" @click="cerrarModalReprogramacion">
      <div class="modal modal-large" @click.stop>
        <div class="modal-header">
          <h2>Reprogramar Turno {{ indiceTurnoActual + 1 }} de {{ turnosAReprogramar.length }}</h2>
          <button @click="cerrarModalReprogramacion" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <div v-if="turnoActual" class="turno-actual-info">
            <h3>Turno Original:</h3>
            <div class="info-box">
              <p><strong>Fecha:</strong> {{ turnoActual.fecha }}</p>
              <p><strong>Hora:</strong> {{ turnoActual.horaInicio }} - {{ turnoActual.horaFin }}</p>
              <p><strong>Cliente:</strong> {{ turnoActual.clienteNombre }} ({{ turnoActual.clienteTelefono }})</p>
              <p><strong>Servicio:</strong> {{ turnoActual.servicioNombre }}</p>
            </div>
          </div>

          <h3>Nueva Fecha y Hora:</h3>

          <!-- Slots Sugeridos -->
          <div v-if="loadingSlots" class="loading-small">Cargando sugerencias...</div>
          <div v-else-if="slotsSugeridos.length > 0" class="slots-sugeridos">
            <p class="slots-titulo">Horarios disponibles sugeridos:</p>
            <div class="slots-grid">
              <button 
                v-for="(slot, idx) in slotsSugeridos.slice(0, 12)" 
                :key="idx"
                @click="seleccionarSlot(slot)"
                :class="['slot-btn', { 'slot-seleccionado': esSlotSeleccionado(slot) }]">
                <div class="slot-fecha">{{ formatearFechaCorta(slot.fecha) }}</div>
                <div class="slot-hora">{{ slot.horaInicio }}</div>
              </button>
            </div>
          </div>

          <!-- Inputs Manuales -->
          <div class="inputs-manuales">
            <p class="inputs-titulo">O elegí manualmente:</p>
            <div class="form-row">
              <div class="form-group">
                <label>Nueva Fecha *</label>
                <input 
                  v-model="reprogramacionActual.nuevaFecha" 
                  type="date" 
                  :min="fechaMinimaReprogramacion"
                  required
                />
              </div>
              <div class="form-group">
                <label>Nueva Hora *</label>
                <input 
                  v-model="reprogramacionActual.nuevaHora" 
                  type="time" 
                  required
                />
              </div>
            </div>
          </div>

          <div v-if="errorReprogramacion" class="error-message">{{ errorReprogramacion }}</div>

          <div class="modal-actions reprogramacion-actions">
            <button 
              v-if="indiceTurnoActual > 0" 
              @click="turnoAnterior" 
              class="btn-secondary">
              ← Anterior
            </button>
            <button 
              @click="cancelarReprogramacion" 
              class="btn-cancel">
              Cancelar Todo
            </button>
            <button 
              @click="siguienteTurno" 
              class="btn-primary"
              :disabled="!reprogramacionActual.nuevaFecha || !reprogramacionActual.nuevaHora">
              {{ indiceTurnoActual < turnosAReprogramar.length - 1 ? 'Siguiente →' : 'Finalizar' }}
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
import { disponibilidadService, type DisponibilidadRequest, type DisponibilidadResponse, diasSemana } from '../services/disponibilidad'
import { bloqueosService, type BloqueoRequest, type BloqueoResponse } from '../services/bloqueos'
import api from '../services/api'

const router = useRouter()
const authStore = useAuthStore()

// Hacer api disponible globalmente para las funciones
;(window as any).apiClient = api

// Estado de navegación
const seccionActiva = ref<'disponibilidad' | 'turnos' | 'bloqueos'>('disponibilidad')

// Estado para Disponibilidad
const disponibilidades = ref<DisponibilidadResponse[]>([])
const horariosEmpresa = ref<any[]>([])
const loadingDisponibilidad = ref(false)
const showModalDisponibilidad = ref(false)
const editingDisponibilidad = ref<DisponibilidadResponse | null>(null)
const errorDisponibilidad = ref('')
const submittingDisponibilidad = ref(false)
const submittingInicializar = ref(false)
const errorInicializar = ref('')

const formDataDisponibilidad = ref<DisponibilidadRequest>({
  diaSemana: '' as any,
  horaInicio: '',
  horaFin: ''
})

const nombresDias: Record<string, string> = {
  LUNES: 'Lunes',
  MARTES: 'Martes',
  MIERCOLES: 'Miércoles',
  JUEVES: 'Jueves',
  VIERNES: 'Viernes',
  SABADO: 'Sábado',
  DOMINGO: 'Domingo'
}

// Agrupar disponibilidades por día
const disponibilidadesAgrupadas = computed(() => {
  const agrupados: Record<string, DisponibilidadResponse[]> = {}
  diasSemana.forEach(dia => {
    agrupados[dia] = disponibilidades.value.filter(d => d.diaSemana === dia)
  })
  return agrupados
})

// Agrupar horarios empresa por día
const horariosEmpresaAgrupados = computed(() => {
  const agrupados: Record<string, any[]> = {}
  diasSemana.forEach(dia => {
    agrupados[dia] = horariosEmpresa.value.filter(h => h.diaSemana === dia)
  })
  return agrupados
})

// Estado para Bloqueos
const bloqueos = ref<BloqueoResponse[]>([])
const loadingBloqueos = ref(false)
const showModalBloqueo = ref(false)
const editingBloqueo = ref<BloqueoResponse | null>(null)
const errorBloqueo = ref('')
const submittingBloqueo = ref(false)

const formDataBloqueo = ref<BloqueoRequest>({
  fechaInicio: '',
  fechaFin: null,
  motivo: null
})

// Estado para Conflictos de Bloqueos
const showModalConflictos = ref(false)
const conflictosData = ref<any>(null)
const bloqueoPendiente = ref<BloqueoRequest | null>(null)

// Estado para Reprogramación
const showModalReprogramacion = ref(false)
const turnosAReprogramar = ref<any[]>([])
const indiceTurnoActual = ref(0)
const reprogramaciones = ref<any[]>([])
const reprogramacionActual = ref({
  nuevaFecha: '',
  nuevaHora: ''
})
const slotsSugeridos = ref<any[]>([])
const loadingSlots = ref(false)
const errorReprogramacion = ref('')

const turnoActual = computed(() => {
  return turnosAReprogramar.value[indiceTurnoActual.value]
})

const fechaMinimaReprogramacion = computed(() => {
  return new Date().toISOString().split('T')[0]
})

const fechaMinima = computed(() => {
  return new Date().toISOString().split('T')[0]
})

// Ordenar bloqueos cronológicamente (próximos primero)
const bloqueosOrdenados = computed(() => {
  return [...bloqueos.value].sort((a, b) => {
    return new Date(a.fechaInicio).getTime() - new Date(b.fechaInicio).getTime()
  })
})

// Estado para Turnos
const turnos = ref<any[]>([])
const loadingTurnos = ref(false)
const filtroTurnos = ref<'hoy' | 'semana' | 'rango'>('semana')
const fechaDesde = ref('')
const fechaHasta = ref('')
const showModalObservaciones = ref(false)
const turnoSeleccionado = ref<any>(null)
const nuevaObservacion = ref('')
const errorObservaciones = ref('')
const submittingObservaciones = ref(false)

const turnosOrdenados = computed(() => {
  return [...turnos.value].sort((a, b) => {
    const fechaA = new Date(a.fecha + 'T' + a.horaInicio).getTime()
    const fechaB = new Date(b.fecha + 'T' + b.horaInicio).getTime()
    return fechaA - fechaB
  })
})

onMounted(async () => {
  await Promise.all([
    cargarDisponibilidad(),
    cargarHorariosEmpresa(),
    cargarBloqueos(),
    cargarTurnos()
  ])
})

// ==================== FUNCIONES DISPONIBILIDAD ====================

async function cargarDisponibilidad() {
  loadingDisponibilidad.value = true
  try {
    disponibilidades.value = await disponibilidadService.obtenerDisponibilidad()
  } catch (err: any) {
    console.error('Error al cargar disponibilidad:', err)
  } finally {
    loadingDisponibilidad.value = false
  }
}

async function cargarHorariosEmpresa() {
  try {
    horariosEmpresa.value = await disponibilidadService.obtenerHorariosEmpresa()
  } catch (err: any) {
    console.error('Error al cargar horarios de empresa:', err)
  }
}

async function inicializarDesdeEmpresa() {
  submittingInicializar.value = true
  errorInicializar.value = ''

  try {
    await disponibilidadService.inicializarDesdeEmpresa()
    await cargarDisponibilidad()
  } catch (err: any) {
    console.error('Error al inicializar desde empresa:', err)
    if (err.response?.data?.mensaje || err.response?.data?.message) {
      errorInicializar.value = err.response.data.mensaje || err.response.data.message
    } else {
      errorInicializar.value = 'Error al inicializar disponibilidad desde la empresa'
    }
  } finally {
    submittingInicializar.value = false
  }
}

function abrirModalDisponibilidad(disponibilidad: DisponibilidadResponse | null = null) {
  if (disponibilidad) {
    editingDisponibilidad.value = disponibilidad
    formDataDisponibilidad.value = {
      diaSemana: disponibilidad.diaSemana,
      horaInicio: disponibilidad.horaInicio,
      horaFin: disponibilidad.horaFin
    }
  } else {
    editingDisponibilidad.value = null
    formDataDisponibilidad.value = {
      diaSemana: '' as any,
      horaInicio: '',
      horaFin: ''
    }
  }
  errorDisponibilidad.value = ''
  showModalDisponibilidad.value = true
}

function cerrarModalDisponibilidad() {
  showModalDisponibilidad.value = false
  editingDisponibilidad.value = null
  formDataDisponibilidad.value = {
    diaSemana: '' as any,
    horaInicio: '',
    horaFin: ''
  }
  errorDisponibilidad.value = ''
}

async function submitFormDisponibilidad() {
  errorDisponibilidad.value = ''
  submittingDisponibilidad.value = true

  try {
    if (editingDisponibilidad.value && editingDisponibilidad.value.id) {
      await disponibilidadService.actualizarDisponibilidad(
        editingDisponibilidad.value.id, 
        formDataDisponibilidad.value
      )
    } else {
      await disponibilidadService.crearDisponibilidad(formDataDisponibilidad.value)
    }
    
    await cargarDisponibilidad()
    cerrarModalDisponibilidad()
  } catch (err: any) {
    console.error('Error al guardar disponibilidad:', err)
    if (err.response?.data?.mensaje || err.response?.data?.message) {
      errorDisponibilidad.value = err.response.data.mensaje || err.response.data.message
    } else {
      errorDisponibilidad.value = 'Error al guardar la disponibilidad'
    }
  } finally {
    submittingDisponibilidad.value = false
  }
}

async function confirmarEliminarDisponibilidad(disponibilidad: DisponibilidadResponse) {
  if (!disponibilidad.id) return
  
  const confirmacion = confirm(
    `¿Estás seguro de eliminar el horario de ${nombresDias[disponibilidad.diaSemana]} (${disponibilidad.horaInicio} - ${disponibilidad.horaFin})?`
  )
  
  if (confirmacion) {
    try {
      await disponibilidadService.eliminarDisponibilidad(disponibilidad.id)
      await cargarDisponibilidad()
    } catch (err: any) {
      console.error('Error al eliminar disponibilidad:', err)
      alert('Error al eliminar la disponibilidad')
    }
  }
}

// ==================== FUNCIONES BLOQUEOS ====================

async function cargarBloqueos() {
  loadingBloqueos.value = true
  try {
    bloqueos.value = await bloqueosService.obtenerBloqueos()
  } catch (err: any) {
    console.error('Error al cargar bloqueos:', err)
  } finally {
    loadingBloqueos.value = false
  }
}

function abrirModalBloqueo(bloqueo: BloqueoResponse | null = null) {
  if (bloqueo) {
    editingBloqueo.value = bloqueo
    formDataBloqueo.value = {
      fechaInicio: bloqueo.fechaInicio,
      fechaFin: bloqueo.fechaFin,
      motivo: bloqueo.motivo
    }
  } else {
    editingBloqueo.value = null
    formDataBloqueo.value = {
      fechaInicio: '',
      fechaFin: null,
      motivo: null
    }
  }
  errorBloqueo.value = ''
  showModalBloqueo.value = true
}

function cerrarModalBloqueo() {
  showModalBloqueo.value = false
  editingBloqueo.value = null
  formDataBloqueo.value = {
    fechaInicio: '',
    fechaFin: null,
    motivo: null
  }
  errorBloqueo.value = ''
}

async function submitFormBloqueo() {
  errorBloqueo.value = ''
  submittingBloqueo.value = true

  try {
    if (editingBloqueo.value) {
      // Actualización no verifica conflictos (por ahora)
      await bloqueosService.actualizarBloqueo(editingBloqueo.value.id, formDataBloqueo.value)
      await cargarBloqueos()
      cerrarModalBloqueo()
    } else {
      // Crear nuevo: verificar conflictos primero
      const conflictos = await bloqueosService.verificarConflictos(formDataBloqueo.value)
      
      if (conflictos.tieneConflictos) {
        // Hay conflictos: mostrar modal de resolución
        bloqueoPendiente.value = { ...formDataBloqueo.value }
        conflictosData.value = conflictos
        showModalBloqueo.value = false
        showModalConflictos.value = true
      } else {
        // Sin conflictos: crear directamente
        await bloqueosService.crearBloqueo(formDataBloqueo.value)
        await cargarBloqueos()
        cerrarModalBloqueo()
        alert('Bloqueo creado exitosamente')
      }
    }
  } catch (err: any) {
    console.error('Error al guardar bloqueo:', err)
    if (err.response?.data?.mensaje || err.response?.data?.message) {
      errorBloqueo.value = err.response.data.mensaje || err.response.data.message
    } else {
      errorBloqueo.value = 'Error al guardar el bloqueo'
    }
  } finally {
    submittingBloqueo.value = false
  }
}

// ==================== FUNCIONES DE CONFLICTOS ====================

function cerrarModalConflictos() {
  showModalConflictos.value = false
  conflictosData.value = null
  bloqueoPendiente.value = null
  showModalBloqueo.value = true // Volver al modal de bloqueo
}

async function resolverConflicto(accion: 'CANCELAR_TODOS' | 'REPROGRAMAR' | 'CANCELAR_FUTUROS') {
  if (!bloqueoPendiente.value || !conflictosData.value) return

  if (accion === 'REPROGRAMAR') {
    // Iniciar proceso de reprogramación
    turnosAReprogramar.value = [...conflictosData.value.turnosConflictivos]
    indiceTurnoActual.value = 0
    reprogramaciones.value = []
    reprogramacionActual.value = { nuevaFecha: '', nuevaHora: '' }
    showModalConflictos.value = false
    showModalReprogramacion.value = true
    cargarSlotsSugeridos()
  } else {
    // Cancelar directamente
    try {
      submittingBloqueo.value = true
      await bloqueosService.crearBloqueoConResolucion({
        bloqueo: bloqueoPendiente.value,
        accion: accion,
        reprogramaciones: undefined
      })
      await cargarBloqueos()
      cerrarModalConflictos()
      showModalBloqueo.value = false
      alert('Bloqueo creado exitosamente. Turnos procesados.')
    } catch (err: any) {
      console.error('Error al resolver conflicto:', err)
      alert('Error al crear el bloqueo: ' + (err.response?.data?.mensaje || err.message))
    } finally {
      submittingBloqueo.value = false
    }
  }
}

// ==================== FUNCIONES DE REPROGRAMACIÓN ====================

function cerrarModalReprogramacion() {
  showModalReprogramacion.value = false
  turnosAReprogramar.value = []
  indiceTurnoActual.value = 0
  reprogramaciones.value = []
  reprogramacionActual.value = { nuevaFecha: '', nuevaHora: '' }
  slotsSugeridos.value = []
  errorReprogramacion.value = ''
  showModalConflictos.value = true // Volver al modal de conflictos
}

function cancelarReprogramacion() {
  const confirmar = confirm('¿Estás seguro de cancelar la reprogramación? Se perderán los cambios.')
  if (confirmar) {
    cerrarModalReprogramacion()
  }
}

async function cargarSlotsSugeridos() {
  if (!turnoActual.value) return

  loadingSlots.value = true
  try {
    slotsSugeridos.value = await bloqueosService.sugerirSlots(turnoActual.value.turnoId, 30)
  } catch (err) {
    console.error('Error al cargar slots:', err)
    slotsSugeridos.value = []
  } finally {
    loadingSlots.value = false
  }
}

function seleccionarSlot(slot: any) {
  reprogramacionActual.value.nuevaFecha = slot.fecha
  reprogramacionActual.value.nuevaHora = slot.horaInicio
}

function esSlotSeleccionado(slot: any) {
  return reprogramacionActual.value.nuevaFecha === slot.fecha &&
         reprogramacionActual.value.nuevaHora === slot.horaInicio
}

function turnoAnterior() {
  if (indiceTurnoActual.value > 0) {
    // Guardar reprogramación actual
    reprogramaciones.value[indiceTurnoActual.value] = {
      turnoId: turnoActual.value.turnoId,
      nuevaFecha: reprogramacionActual.value.nuevaFecha,
      nuevaHora: reprogramacionActual.value.nuevaHora
    }

    // Ir al anterior
    indiceTurnoActual.value--

    // Cargar datos del anterior (si existen)
    if (reprogramaciones.value[indiceTurnoActual.value]) {
      reprogramacionActual.value = {
        nuevaFecha: reprogramaciones.value[indiceTurnoActual.value].nuevaFecha,
        nuevaHora: reprogramaciones.value[indiceTurnoActual.value].nuevaHora
      }
    } else {
      reprogramacionActual.value = { nuevaFecha: '', nuevaHora: '' }
    }

    cargarSlotsSugeridos()
  }
}

async function siguienteTurno() {
  errorReprogramacion.value = ''

  // Validar que se haya elegido fecha y hora
  if (!reprogramacionActual.value.nuevaFecha || !reprogramacionActual.value.nuevaHora) {
    errorReprogramacion.value = 'Debe seleccionar una fecha y hora'
    return
  }

  // Guardar reprogramación actual
  reprogramaciones.value[indiceTurnoActual.value] = {
    turnoId: turnoActual.value.turnoId,
    nuevaFecha: reprogramacionActual.value.nuevaFecha,
    nuevaHora: reprogramacionActual.value.nuevaHora
  }

  if (indiceTurnoActual.value < turnosAReprogramar.value.length - 1) {
    // Ir al siguiente turno
    indiceTurnoActual.value++

    // Cargar datos del siguiente (si ya se había completado)
    if (reprogramaciones.value[indiceTurnoActual.value]) {
      reprogramacionActual.value = {
        nuevaFecha: reprogramaciones.value[indiceTurnoActual.value].nuevaFecha,
        nuevaHora: reprogramaciones.value[indiceTurnoActual.value].nuevaHora
      }
    } else {
      reprogramacionActual.value = { nuevaFecha: '', nuevaHora: '' }
    }

    cargarSlotsSugeridos()
  } else {
    // Último turno: finalizar reprogramación
    await finalizarReprogramacion()
  }
}

async function finalizarReprogramacion() {
  if (!bloqueoPendiente.value) return

  try {
    submittingBloqueo.value = true
    await bloqueosService.crearBloqueoConResolucion({
      bloqueo: bloqueoPendiente.value,
      accion: 'REPROGRAMAR',
      reprogramaciones: reprogramaciones.value
    })

    await cargarBloqueos()
    showModalReprogramacion.value = false
    showModalConflictos.value = false
    showModalBloqueo.value = false
    alert('Bloqueo creado y turnos reprogramados exitosamente')

    // Limpiar estado
    turnosAReprogramar.value = []
    indiceTurnoActual.value = 0
    reprogramaciones.value = []
    bloqueoPendiente.value = null
    conflictosData.value = null
  } catch (err: any) {
    console.error('Error al finalizar reprogramación:', err)
    errorReprogramacion.value = 'Error: ' + (err.response?.data?.mensaje || err.message)
  } finally {
    submittingBloqueo.value = false
  }
}

function formatearFechaCorta(fecha: string) {
  const date = new Date(fecha + 'T00:00:00')
  return date.toLocaleDateString('es-AR', { 
    day: '2-digit', 
    month: 'short'
  })
}

async function confirmarEliminarBloqueo(bloqueo: BloqueoResponse) {
  const rango = bloqueo.fechaFin && bloqueo.fechaFin !== bloqueo.fechaInicio
    ? `${formatearFecha(bloqueo.fechaInicio)} - ${formatearFecha(bloqueo.fechaFin)}`
    : formatearFecha(bloqueo.fechaInicio)
  
  const confirmacion = confirm(`¿Estás seguro de eliminar el bloqueo del ${rango}?`)
  
  if (confirmacion) {
    try {
      await bloqueosService.eliminarBloqueo(bloqueo.id)
      await cargarBloqueos()
    } catch (err: any) {
      console.error('Error al eliminar bloqueo:', err)
      alert('Error al eliminar el bloqueo')
    }
  }
}

function formatearFecha(fecha: string): string {
  const date = new Date(fecha + 'T00:00:00')
  return date.toLocaleDateString('es-AR', { 
    day: '2-digit', 
    month: '2-digit', 
    year: 'numeric' 
  })
}

// ==================== FUNCIONES TURNOS ====================

async function cargarTurnos() {
  try {
    loadingTurnos.value = true
    const params: any = {}
    
    if (filtroTurnos.value === 'hoy') {
      params.fecha = new Date().toISOString().split('T')[0]
    } else if (filtroTurnos.value === 'semana') {
      const hoy = new Date()
      const enUnaSemana = new Date()
      enUnaSemana.setDate(hoy.getDate() + 7)
      params.fechaDesde = hoy.toISOString().split('T')[0]
      params.fechaHasta = enUnaSemana.toISOString().split('T')[0]
    } else if (filtroTurnos.value === 'rango' && fechaDesde.value && fechaHasta.value) {
      params.fechaDesde = fechaDesde.value
      params.fechaHasta = fechaHasta.value
    }

    const response = await (window as any).apiClient.obtenerTurnosProfesional(params)
    turnos.value = response.data.datos
  } catch (error: any) {
    console.error('Error al cargar turnos:', error)
    alert('Error al cargar turnos: ' + (error.response?.data?.mensaje || error.message))
  } finally {
    loadingTurnos.value = false
  }
}

function cambiarFiltroTurnos(filtro: 'hoy' | 'semana' | 'rango') {
  filtroTurnos.value = filtro
  if (filtro !== 'rango') {
    cargarTurnos()
  }
}

function puedesCambiarEstado(turno: any): boolean {
  return turno.estado !== 'CANCELADO' && turno.estado !== 'ATENDIDO'
}

function estadosDisponibles(turno: any) {
  const estados = []
  
  if (turno.estado === 'CREADO' || turno.estado === 'PENDIENTE_CONFIRMACION') {
    estados.push({ valor: 'CONFIRMADO', label: 'Confirmar' })
    estados.push({ valor: 'CANCELADO', label: 'Cancelar' })
  } else if (turno.estado === 'CONFIRMADO') {
    estados.push({ valor: 'ATENDIDO', label: 'Atendido' })
    estados.push({ valor: 'NO_ASISTIO', label: 'No Asistió' })
    estados.push({ valor: 'CANCELADO', label: 'Cancelar' })
  }
  
  return estados
}

async function cambiarEstado(turno: any, nuevoEstado: string) {
  if (!nuevoEstado) return
  
  try {
    const response = await (window as any).apiClient.cambiarEstadoTurno(turno.id, {
      nuevoEstado,
      observaciones: null
    })
    
    // Actualizar turno en la lista
    const index = turnos.value.findIndex(t => t.id === turno.id)
    if (index !== -1) {
      turnos.value[index] = response.data.datos
    }
    
    alert('Estado actualizado exitosamente')
  } catch (error: any) {
    console.error('Error al cambiar estado:', error)
    alert('Error al cambiar estado: ' + (error.response?.data?.mensaje || error.message))
  }
}

function abrirModalObservaciones(turno: any) {
  turnoSeleccionado.value = turno
  nuevaObservacion.value = ''
  errorObservaciones.value = ''
  showModalObservaciones.value = true
}

function cerrarModalObservaciones() {
  showModalObservaciones.value = false
  turnoSeleccionado.value = null
  nuevaObservacion.value = ''
  errorObservaciones.value = ''
}

async function submitObservaciones() {
  if (!turnoSeleccionado.value || !nuevaObservacion.value.trim()) {
    errorObservaciones.value = 'La observación es obligatoria'
    return
  }

  try {
    submittingObservaciones.value = true
    errorObservaciones.value = ''
    
    const response = await (window as any).apiClient.agregarObservacionesTurno(
      turnoSeleccionado.value.id,
      { observaciones: nuevaObservacion.value }
    )
    
    // Actualizar turno en la lista
    const index = turnos.value.findIndex(t => t.id === turnoSeleccionado.value.id)
    if (index !== -1) {
      turnos.value[index] = response.data.datos
    }
    
    cerrarModalObservaciones()
    alert('Observación agregada exitosamente')
  } catch (error: any) {
    errorObservaciones.value = error.response?.data?.mensaje || 'Error al agregar observación'
  } finally {
    submittingObservaciones.value = false
  }
}

function formatearFechaLegible(fecha: string): string {
  const date = new Date(fecha + 'T00:00:00')
  return date.toLocaleDateString('es-AR', { 
    weekday: 'short',
    day: '2-digit', 
    month: 'short'
  })
}

function cerrarSesion() {
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
</script>

<style scoped>
.profesional-view {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 1.5rem 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.tabs-navigation {
  background: white;
  border-bottom: 2px solid #e2e8f0;
  padding: 0 2rem;
  display: flex;
  gap: 0.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.tab-btn {
  padding: 1rem 1.5rem;
  border: none;
  background: transparent;
  color: #4a5568;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  transition: all 0.3s ease;
  position: relative;
}

.tab-btn:hover {
  color: #667eea;
  background: #f7fafc;
}

.tab-btn.active {
  color: #667eea;
  border-bottom-color: #667eea;
  background: #f7fafc;
}

.header {
  background-color: #667eea;
  color: white;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  margin: 0;
  font-size: 1.75rem;
}

.btn-logout {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-logout:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.section {
  background-color: white;
  border-radius: 8px;
  padding: 2rem;
  margin-bottom: 2rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #1f2937;
}

.btn-primary {
  background-color: #667eea;
  color: white;
  border: none;
  padding: 0.625rem 1.25rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background-color: #5568d3;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-large {
  font-size: 1.125rem;
  padding: 1rem 2rem;
  margin: 1.5rem 0;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
}

/* Empty State Disponibilidad */
.empty-state-disponibilidad {
  text-align: center;
  padding: 1rem 0;
}

.info-card {
  background-color: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  padding: 1.5rem;
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  text-align: left;
}

.info-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.info-content h3 {
  margin: 0 0 0.5rem 0;
  color: #1e40af;
  font-size: 1.125rem;
}

.info-content p {
  margin: 0;
  color: #1e3a8a;
}

/* Horarios Referencia */
.horarios-referencia {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 2px dashed #e5e7eb;
}

.horarios-referencia h3 {
  color: #6b7280;
  font-size: 1rem;
  margin-bottom: 1rem;
}

.horarios-referencia-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
}

.dia-card-ref {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 0.75rem;
}

.dia-name {
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
}

.horarios-ref-list {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.horario-ref-item {
  font-size: 0.875rem;
  color: #6b7280;
}

.sin-horarios-ref {
  font-size: 0.875rem;
  color: #9ca3af;
  font-style: italic;
}

/* Disponibilidad Container */
.disponibilidad-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.dia-card {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.dia-header {
  background-color: #667eea;
  color: white;
  padding: 0.75rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dia-header h3 {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 600;
}

.count-badge {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  padding: 0.25rem 0.5rem;
  border-radius: 10px;
  font-size: 0.75rem;
}

.dia-body {
  padding: 0.75rem;
}

.horarios-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.horario-item {
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 0.5rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.horario-time {
  font-weight: 500;
  color: #1f2937;
  font-size: 0.875rem;
}

.horario-actions {
  display: flex;
  gap: 0.25rem;
}

.btn-edit-small,
.btn-delete-small {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-edit-small {
  background-color: #f3f4f6;
  color: #4b5563;
}

.btn-edit-small:hover {
  background-color: #e5e7eb;
}

.btn-delete-small {
  background-color: #fee2e2;
  color: #dc2626;
}

.btn-delete-small:hover {
  background-color: #fecaca;
}

.sin-horarios {
  color: #9ca3af;
  font-size: 0.875rem;
  text-align: center;
  padding: 0.5rem;
}

/* Horarios Empresa Collapsible */
.horarios-empresa-collapsible {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1.5rem;
}

.horarios-empresa-collapsible summary {
  cursor: pointer;
  font-weight: 600;
  color: #6b7280;
  user-select: none;
}

.horarios-empresa-collapsible summary:hover {
  color: #374151;
}

.horarios-referencia-grid-small {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.dia-ref-inline {
  font-size: 0.875rem;
  color: #6b7280;
}

.dia-ref-inline strong {
  color: #374151;
  margin-right: 0.5rem;
}

.text-muted {
  color: #9ca3af;
  font-style: italic;
}

/* Bloqueos List */
.bloqueos-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.bloqueo-item {
  background-color: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bloqueo-info {
  flex: 1;
}

.bloqueo-fechas {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  margin-bottom: 0.5rem;
}

.fecha-badge {
  background-color: #667eea;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 4px;
  font-size: 0.875rem;
  font-weight: 500;
}

.bloqueo-motivo {
  color: #6b7280;
  font-size: 0.875rem;
}

.bloqueo-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-edit,
.btn-delete {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-edit {
  background-color: #f3f4f6;
  color: #4b5563;
}

.btn-edit:hover {
  background-color: #e5e7eb;
}

.btn-delete {
  background-color: #fee2e2;
  color: #dc2626;
}

.btn-delete:hover {
  background-color: #fecaca;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: #6b7280;
}

.empty-state p {
  margin: 0.5rem 0;
}

.empty-hint {
  font-size: 0.875rem;
  color: #9ca3af;
}

/* Modal Overlay */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 1rem;
}

.modal {
  background-color: white;
  border-radius: 8px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
}

.modal-small {
  max-width: 500px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  color: #1f2937;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
  line-height: 1;
}

.btn-close:hover {
  color: #1f2937;
}

.modal-form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.625rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-group textarea {
  resize: vertical;
  font-family: inherit;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-hint {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.error-message {
  background-color: #fee2e2;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 6px;
  margin-top: 1rem;
  font-size: 0.875rem;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-cancel {
  background-color: #f3f4f6;
  color: #4b5563;
  border: none;
  padding: 0.625rem 1.25rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-cancel:hover {
  background-color: #e5e7eb;
}

.btn-submit {
  background-color: #667eea;
  color: white;
  border: none;
  padding: 0.625rem 1.25rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background-color: #5568d3;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }

  .section {
    padding: 1.5rem 1rem;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .disponibilidad-grid {
    grid-template-columns: 1fr;
  }

  .horarios-referencia-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .modal {
    margin: 1rem;
  }

  .bloqueo-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .bloqueo-actions {
    width: 100%;
    justify-content: flex-end;
  }
}

/* ==================== ESTILOS TURNOS ==================== */

.filtros-turnos {
  display: flex;
  gap: 10px;
}

.btn-filtro {
  padding: 8px 16px;
  border: 2px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn-filtro:hover {
  background: #f0f2ff;
}

.btn-filtro.active {
  background: #667eea;
  color: white;
}

.rango-fechas {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.rango-fechas input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-primary-small {
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}

.btn-primary-small:hover {
  background: #5568d3;
}

.turnos-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.turno-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.turno-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.turno-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid #f0f0f0;
}

.turno-fecha {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.fecha-dia {
  font-size: 1.1rem;
  font-weight: 600;
  color: #2d3748;
  text-transform: capitalize;
}

.turno-hora {
  font-size: 0.95rem;
  color: #667eea;
  font-weight: 500;
}

.estado-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
}

.estado-creado {
  background: #e3f2fd;
  color: #1976d2;
}

.estado-pendiente_confirmacion {
  background: #fff3e0;
  color: #f57c00;
}

.estado-confirmado {
  background: #e8f5e9;
  color: #388e3c;
}

.estado-cancelado {
  background: #ffebee;
  color: #d32f2f;
}

.estado-atendido {
  background: #f3e5f5;
  color: #7b1fa2;
}

.estado-no_asistio {
  background: #fce4ec;
  color: #c2185b;
}

.turno-body {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.turno-info-row {
  font-size: 0.95rem;
  color: #4a5568;
}

.turno-info-row strong {
  color: #2d3748;
  margin-right: 8px;
}

.turno-observaciones {
  margin-top: 0.5rem;
  padding: 0.75rem;
  background: #f7fafc;
  border-left: 3px solid #667eea;
  border-radius: 4px;
}

.turno-observaciones p {
  margin: 0.5rem 0 0 0;
  font-size: 0.9rem;
  color: #4a5568;
  white-space: pre-wrap;
}

.turno-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.select-estado {
  flex: 1;
  padding: 8px 12px;
  border: 2px solid #667eea;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  background: white;
  color: #667eea;
  font-weight: 500;
}

.select-estado:focus {
  outline: none;
  border-color: #5568d3;
}

.btn-secondary-small {
  padding: 8px 16px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  white-space: nowrap;
}

.btn-secondary-small:hover {
  background: #38a169;
}

.turno-info-modal {
  background: #f7fafc;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.turno-info-modal p {
  margin: 0.5rem 0;
  font-size: 0.95rem;
  color: #4a5568;
}

@media (max-width: 768px) {
  .filtros-turnos {
    flex-wrap: wrap;
  }

  .turno-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.5rem;
  }

  .turno-actions {
    flex-direction: column;
    width: 100%;
  }

  .select-estado {
    width: 100%;
  }
}

/* Estilos para modales de resolución de conflictos */
.modal-medium {
  max-width: 700px;
}

.modal-large {
  max-width: 900px;
}

.conflicto-mensaje {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  color: #856404;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.conflicto-mensaje span:first-child {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.turnos-conflictivos {
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 1.5rem;
  padding: 0.5rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.turnos-conflictivos h4 {
  margin: 0 0 1rem 0;
  color: #2d3748;
  font-size: 1rem;
}

.turnos-conflictivos ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.turnos-conflictivos li {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 1rem;
  margin-bottom: 0.75rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.turnos-conflictivos li strong {
  display: block;
  margin-bottom: 0.5rem;
  color: #2d3748;
  font-size: 1rem;
}

.turnos-conflictivos li p {
  margin: 0.25rem 0;
  color: #4a5568;
  font-size: 0.9rem;
}

.opciones-resolucion {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1.5rem;
}

.opciones-resolucion h4 {
  margin: 0 0 0.5rem 0;
  color: #2d3748;
  font-size: 1rem;
}

.opcion-btn {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  font-size: 0.95rem;
}

.opcion-btn:hover {
  border-color: #4299e1;
  background: #ebf8ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.opcion-btn .icono {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.opcion-btn .contenido {
  flex: 1;
}

.opcion-btn .titulo {
  display: block;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 0.25rem;
}

.opcion-btn .descripcion {
  display: block;
  color: #718096;
  font-size: 0.85rem;
}

.opcion-btn.cancelar:hover {
  border-color: #f56565;
  background: #fff5f5;
}

.opcion-btn.reprogramar:hover {
  border-color: #48bb78;
  background: #f0fff4;
}

.opcion-btn.cancelar-futuros:hover {
  border-color: #ed8936;
  background: #fffaf0;
}

/* Estilos para modal de reprogramación */
.turno-actual-info {
  background: #ebf8ff;
  border: 1px solid #4299e1;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1.5rem;
}

.turno-actual-info h4 {
  margin: 0 0 0.75rem 0;
  color: #2c5282;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.info-box {
  background: white;
  border-radius: 6px;
  padding: 0.75rem;
}

.info-box p {
  margin: 0.4rem 0;
  color: #2d3748;
  font-size: 0.9rem;
}

.info-box strong {
  color: #2c5282;
  margin-right: 0.5rem;
}

.slots-sugeridos {
  margin-bottom: 1.5rem;
}

.slots-sugeridos h4 {
  margin: 0 0 0.75rem 0;
  color: #2d3748;
  font-size: 1rem;
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 0.75rem;
  max-height: 300px;
  overflow-y: auto;
  padding: 0.5rem;
  background: #f7fafc;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.slot-btn {
  padding: 0.75rem;
  background: white;
  border: 2px solid #cbd5e0;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
  font-size: 0.85rem;
}

.slot-btn:hover {
  border-color: #4299e1;
  background: #ebf8ff;
  transform: translateY(-1px);
}

.slot-btn.slot-seleccionado {
  border-color: #48bb78;
  background: #f0fff4;
  font-weight: 600;
}

.slot-btn p {
  margin: 0.25rem 0;
}

.slot-btn .dia {
  color: #718096;
  font-size: 0.75rem;
  margin-bottom: 0.25rem;
}

.slot-btn .fecha {
  color: #2d3748;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.slot-btn .hora {
  color: #4299e1;
  font-size: 0.8rem;
}

.o-bien {
  text-align: center;
  margin: 1rem 0;
  color: #718096;
  font-size: 0.9rem;
  position: relative;
}

.o-bien::before,
.o-bien::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background: #e2e8f0;
}

.o-bien::before {
  left: 0;
}

.o-bien::after {
  right: 0;
}

.reprogramacion-form {
  background: #f7fafc;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.reprogramacion-form .form-group {
  margin-bottom: 1rem;
}

.reprogramacion-form label {
  display: block;
  margin-bottom: 0.5rem;
  color: #2d3748;
  font-weight: 500;
  font-size: 0.9rem;
}

.reprogramacion-form input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #cbd5e0;
  border-radius: 6px;
  font-size: 0.9rem;
}

.reprogramacion-form input:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

.reprogramacion-actions {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e2e8f0;
}

.reprogramacion-actions .btn-group {
  display: flex;
  gap: 0.75rem;
}

.btn-navegacion {
  padding: 0.625rem 1rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-anterior {
  background: #e2e8f0;
  color: #2d3748;
}

.btn-anterior:hover:not(:disabled) {
  background: #cbd5e0;
}

.btn-siguiente {
  background: #4299e1;
  color: white;
}

.btn-siguiente:hover:not(:disabled) {
  background: #3182ce;
}

.btn-finalizar {
  background: #48bb78;
  color: white;
}

.btn-finalizar:hover {
  background: #38a169;
}

.btn-navegacion:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .modal-medium,
  .modal-large {
    max-width: 95vw;
  }

  .slots-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 0.5rem;
  }

  .opciones-resolucion {
    gap: 0.75rem;
  }

  .opcion-btn {
    padding: 0.875rem 1rem;
  }

  .reprogramacion-actions {
    flex-direction: column;
  }

  .reprogramacion-actions .btn-group {
    width: 100%;
  }

  .btn-navegacion {
    flex: 1;
  }
}
</style>
