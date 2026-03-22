<template>
  <div v-if="show" class="modal-overlay" @click="handleCerrar">
    <div class="modal modal-medium" @click.stop>
      <div class="modal-header">
        <h2 class="flex items-center gap-2">
          <svg class="h-5 w-5 text-amber-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m0 3.75h.008v.008H12v-.008Zm8.25-.75a8.25 8.25 0 1 1-16.5 0 8.25 8.25 0 0 1 16.5 0Z" />
          </svg>
          Conflictos con Turnos Existentes
        </h2>
        <button @click="handleCerrar" class="btn-close">&times;</button>
      </div>
      <div class="modal-body">
        <p class="conflicto-mensaje">
          Se encontraron <strong>{{ conflictosData?.cantidadConflictos }}</strong> turno(s)
          que están dentro del rango del bloqueo.
        </p>

        <!-- Banner de advertencia por violación de política (informativo) -->
        <div v-if="conflictosData?.hayViolacionPolitica" class="politica-warning">
          <span class="politica-icono">
            <svg class="h-5 w-5 text-amber-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 0 0 5.454-1.31A8.967 8.967 0 0 1 18 9.75v-.7V9a6 6 0 1 0-12 0v.05c0 .232 0 .467-.002.7a8.967 8.967 0 0 1-2.312 6.022c1.733.64 3.56 1.08 5.454 1.31m5.717 0a24.255 24.255 0 0 1-5.717 0m5.717 0a3 3 0 1 1-5.717 0" />
            </svg>
          </span>
          <div class="politica-texto">
            <strong>Advertencia: algunos turnos violan la política de cancelación.</strong>
            <p>Se recomienda informar al cliente con anticipación.</p>
          </div>
        </div>

        <div class="turnos-conflictivos">
          <div
            v-for="turno in conflictosData?.turnosConflictivos"
            :key="turno.turnoId"
            class="turno-conflicto-item"
          >
            <div class="turno-conflicto-info">
              <strong>{{ turno.fecha }} {{ turno.horaInicio }}-{{ turno.horaFin }}</strong>
              <p>{{ turno.servicioNombre }}</p>
              <p class="cliente-info">{{ turno.clienteNombre }} - {{ turno.clienteTelefono }}</p>
              <span :class="['estado-badge-small', `estado-${turno.estado.toLowerCase()}`]">
                {{ turno.estado }}
              </span>
              <span v-if="turno.violaPolitica" class="politica-badge" title="Viola política de cancelación">
                <span class="flex items-center gap-1">
                  <svg class="h-4 w-4 text-amber-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m0 3.75h.008v.008H12v-.008Zm8.25-.75a8.25 8.25 0 1 1-16.5 0 8.25 8.25 0 0 1 16.5 0Z" />
                  </svg>
                  Viola política
                </span>
              </span>
            </div>
            <p v-if="turno.violaPolitica && turno.descripcionViolacion" class="descripcion-violacion">
              {{ turno.descripcionViolacion }}
            </p>
          </div>
        </div>

        <!-- Aviso: opción de reprogramar desde Mis Turnos -->
        <div class="reprogramar-info">
          <span class="reprogramar-info-icono">
            <svg class="h-5 w-5 text-blue-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 18v-5.25m0 0a3.75 3.75 0 1 0-3.75-3.75c0 1.197.56 2.266 1.433 2.953.517.407.817 1.028.817 1.672V18m1.5 0h-3m3 0h3" />
            </svg>
          </span>
          <p>
            Si querés reprogramar los turnos, podés hacerlo desde
            <strong>Mis Turnos</strong> una vez creado el bloqueo.
          </p>
        </div>

        <div v-if="errorCancelar" class="error-message">{{ errorCancelar }}</div>

        <div class="modal-actions">
          <button @click="handleCerrar" class="btn-secondary" :disabled="submitting">
            Volver
          </button>
          <button @click="cancelarEnRango" class="btn-cancelar" :disabled="submitting">
            <span v-if="submitting">Procesando...</span>
            <span v-else class="flex items-center gap-2">
              <svg class="h-4 w-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12" />
              </svg>
              Cancelar Turnos en el Rango
            </span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { bloqueosService, type BloqueoRequest, type ConflictosBloqueoResponse } from '../services/bloqueos'
import { useToastStore } from '../composables/useToast'

const props = defineProps<{
  show: boolean
  conflictosData: ConflictosBloqueoResponse | null
  bloqueoPendiente: BloqueoRequest | null
}>()

const emit = defineEmits<{
  cerrar: []
  bloqueoCreado: []
}>()

const toastStore = useToastStore()
const submitting = ref(false)
const errorCancelar = ref('')

function handleCerrar() {
  errorCancelar.value = ''
  emit('cerrar')
}

async function cancelarEnRango() {
  if (!props.bloqueoPendiente) return
  errorCancelar.value = ''
  try {
    submitting.value = true
    const result = await bloqueosService.crearBloqueoConResolucion({
      bloqueo: props.bloqueoPendiente,
      accion: 'CANCELAR_EN_RANGO'
    })
    // Fuente única de verdad: mensaje que devuelve el backend.
    // ProfesionalView.onBloqueoCreado() NO debe mostrar un segundo toast.
    toastStore.showSuccess(result.mensaje || 'Bloqueo creado. Los turnos en conflicto fueron cancelados.', 5000)
    emit('bloqueoCreado')
  } catch (err: any) {
    errorCancelar.value = 'Error al crear el bloqueo: ' + (err.response?.data?.mensaje || err.message)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* ==================== INFRAESTRUCTURA MODAL ==================== */

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

.modal-medium {
  max-width: 620px;
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

.modal-body {
  padding: 1.5rem;
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

/* ==================== BOTONES ==================== */

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-secondary {
  background-color: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 0.625rem 1.25rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #e5e7eb;
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-cancelar {
  background-color: #dc2626;
  color: white;
  border: none;
  padding: 0.625rem 1.25rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-cancelar:hover:not(:disabled) {
  background-color: #b91c1c;
}

.btn-cancelar:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ==================== LISTA DE CONFLICTOS ==================== */

.conflicto-mensaje {
  color: #374151;
  margin-bottom: 1rem;
  font-size: 0.95rem;
}

.turnos-conflictivos {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
  max-height: 280px;
  overflow-y: auto;
  padding-right: 0.25rem;
}

.turno-conflicto-item {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 0.875rem;
}

.turno-conflicto-info strong {
  display: block;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.turno-conflicto-info p {
  margin: 0.2rem 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.cliente-info {
  font-size: 0.85rem;
  color: #6b7280;
}

.estado-badge-small {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.estado-pendiente_confirmacion { background: #fff3e0; color: #f57c00; }
.estado-pendiente_pago         { background: #fff9c4; color: #8a6d00; }
.estado-confirmado             { background: #e8f5e9; color: #388e3c; }
.estado-cancelado              { background: #ffebee; color: #d32f2f; }
.estado-atendido               { background: #f3e5f5; color: #7b1fa2; }
.estado-no_asistio             { background: #fce4ec; color: #c2185b; }

/* ==================== ADVERTENCIA DE POLÍTICA ==================== */

.politica-warning {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  background: #fffbeb;
  border: 1px solid #f59e0b;
  border-radius: 8px;
  padding: 0.875rem 1rem;
  margin-bottom: 1rem;
}

.politica-icono {
  font-size: 1.25rem;
  flex-shrink: 0;
}

.politica-texto strong {
  display: block;
  color: #92400e;
  font-size: 0.9rem;
}

.politica-texto p {
  color: #78350f;
  font-size: 0.825rem;
  margin: 0.25rem 0 0;
}

.politica-badge {
  display: inline-block;
  font-size: 0.75rem;
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fcd34d;
  border-radius: 4px;
  padding: 1px 6px;
  margin-left: 0.5rem;
  vertical-align: middle;
}

.descripcion-violacion {
  font-size: 0.8rem;
  color: #b45309;
  background: #fef9ec;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  margin: 0.25rem 0 0;
}

/* ==================== AVISO REPROGRAMAR ==================== */

.reprogramar-info {
  display: flex;
  align-items: flex-start;
  gap: 0.625rem;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  padding: 0.75rem 1rem;
  margin-bottom: 0.5rem;
}

.reprogramar-info-icono {
  font-size: 1.1rem;
  flex-shrink: 0;
  margin-top: 1px;
}

.reprogramar-info p {
  color: #1e40af;
  font-size: 0.85rem;
  margin: 0;
  line-height: 1.4;
}

.reprogramar-info strong {
  color: #1d4ed8;
}

/* ==================== ERROR ==================== */

.error-message {
  background-color: #fee2e2;
  color: #dc2626;
  padding: 0.75rem;
  border-radius: 6px;
  margin-top: 0.75rem;
  font-size: 0.875rem;
}
</style>
