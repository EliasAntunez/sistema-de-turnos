<template>
  <div class="notification-bell-container">
    <!-- Botón de la campanita -->
    <button 
      @click="toggleDropdown" 
      class="bell-button"
      :class="{ 'has-notifications': store.tieneNotificacionesNoLeidas }"
      title="Notificaciones">
      <span class="bell-icon">🔔</span>
      <span 
        v-if="store.contadorNoLeidas > 0" 
        class="badge"
        :class="{ 'badge-pulse': hayNotificacionesRecientes }">
        {{ store.contadorNoLeidas > MAX_BADGE_NUMBER ? '99+' : store.contadorNoLeidas }}
      </span>
    </button>

    <!-- Dropdown de notificaciones -->
    <Transition name="dropdown">
      <div v-if="mostrarDropdown" class="notifications-dropdown">
        <!-- Header -->
        <div class="dropdown-header">
          <h3>Notificaciones</h3>
          <div class="header-actions">
            <button 
              v-if="store.tieneNotificacionesNoLeidas"
              @click="marcarTodasLeidas" 
              class="btn-mark-all"
              title="Marcar todas como leídas">
              ✓ Marcar todas
            </button>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="store.cargando" class="loading-container">
          <div class="spinner"></div>
          <p>Cargando notificaciones...</p>
        </div>

        <!-- Lista de notificaciones -->
        <div v-else-if="store.notificaciones && store.notificaciones.length > 0" class="notifications-list">
          <NotificationItem
            v-for="notificacion in notificacionesMostradas"
            :key="notificacion.id"
            :notificacion="notificacion"
            @click="handleNotificationClick(notificacion)"
          />
        </div>

        <!-- Estado vacío -->
        <div v-else class="empty-state">
          <span class="empty-icon">📭</span>
          <p>No hay notificaciones</p>
        </div>

        <!-- Footer -->
        <div class="dropdown-footer">
          <button @click="verTodas" class="btn-view-all">
            Ver todas las notificaciones
          </button>
        </div>
      </div>
    </Transition>

    <!-- Overlay para cerrar el dropdown al hacer clic fuera -->
    <div 
      v-if="mostrarDropdown" 
      class="dropdown-overlay" 
      @click="cerrarDropdown">
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useNotificacionesStore } from '../stores/notificaciones'
import NotificationItem from './NotificationItem.vue'
import type { Notificacion } from '../services/notificaciones'

// ✅ B4: Constantes para evitar magic numbers
const MAX_NOTIFICATIONS_SHOWN = 10  // Máximo en dropdown
const MAX_BADGE_NUMBER = 99         // Máximo mostrado en badge

const store = useNotificacionesStore()
const mostrarDropdown = ref(false)
const hayNotificacionesRecientes = ref(false)

// Mostrar solo las últimas N notificaciones en el dropdown
const notificacionesMostradas = computed(() => 
  store.notificaciones?.slice(0, MAX_NOTIFICATIONS_SHOWN) || []
)

/**
 * Toggle del dropdown
 */
function toggleDropdown() {
  mostrarDropdown.value = !mostrarDropdown.value
  
  if (mostrarDropdown.value) {
    hayNotificacionesRecientes.value = false
  }
}

/**
 * Cerrar dropdown
 */
function cerrarDropdown() {
  mostrarDropdown.value = false
}

/**
 * Manejar clic en una notificación
 */
async function handleNotificationClick(notificacion: Notificacion) {
  // Marcar como leída si no lo está
  if (!notificacion.leida) {
    try {
      await store.marcarComoLeida(notificacion.id)
    } catch (error) {
      console.error('Error al marcar notificación:', error)
    }
  }

  // Cerrar dropdown
  cerrarDropdown()

  // Aquí podrías navegar a una vista específica o mostrar detalles
  // Por ejemplo, si tiene turnoId, navegar a ese turno
  if (notificacion.turnoId) {
    console.log('Navegar a turno:', notificacion.turnoId)
    // router.push(`/profesional/turnos/${notificacion.turnoId}`)
  }
}

/**
 * Marcar todas como leídas
 */
async function marcarTodasLeidas() {
  try {
    await store.marcarTodasComoLeidas()
  } catch (error) {
    console.error('Error al marcar todas:', error)
  }
}

/**
 * Ver todas las notificaciones
 */
function verTodas() {
  cerrarDropdown()
  // Aquí podrías navegar a una vista de historial completo
  console.log('Ver todas las notificaciones')
}

/**
 * Listener para nuevas notificaciones
 */
function onNuevaNotificacion() {
  hayNotificacionesRecientes.value = true
  
  // Remover el efecto después de 5 segundos
  setTimeout(() => {
    hayNotificacionesRecientes.value = false
  }, 5000)
}

// Lifecycle
onMounted(() => {
  // Escuchar eventos de nuevas notificaciones
  window.addEventListener('nueva-notificacion', onNuevaNotificacion as EventListener)
})

onUnmounted(() => {
  window.removeEventListener('nueva-notificacion', onNuevaNotificacion as EventListener)
})
</script>

<style scoped>
.notification-bell-container {
  position: relative;
}

.bell-button {
  position: relative;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bell-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.bell-button.has-notifications {
  animation: ring 2s ease-in-out infinite;
}

.bell-icon {
  font-size: 1.5rem;
  display: block;
}

.badge {
  position: absolute;
  top: 0;
  right: 0;
  background: #ef4444;
  color: white;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.125rem 0.375rem;
  border-radius: 10px;
  min-width: 1.25rem;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.badge-pulse {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes ring {
  0%, 100% {
    transform: rotate(0deg);
  }
  10%, 30% {
    transform: rotate(-15deg);
  }
  20%, 40% {
    transform: rotate(15deg);
  }
  50% {
    transform: rotate(0deg);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

.dropdown-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 998;
}

.notifications-dropdown {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  width: 400px;
  max-height: 600px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  z-index: 999;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dropdown-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f9fafb;
}

.dropdown-header h3 {
  margin: 0;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1f2937;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-mark-all {
  background: none;
  border: none;
  color: #667eea;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-mark-all:hover {
  background: #eef2ff;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  color: #6b7280;
}

.spinner {
  width: 2rem;
  height: 2rem;
  border: 3px solid #e5e7eb;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.notifications-list {
  flex: 1;
  overflow-y: auto;
  max-height: 450px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  color: #9ca3af;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.empty-state p {
  margin: 0;
  font-size: 0.875rem;
}

.dropdown-footer {
  padding: 0.75rem 1.25rem;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
}

.btn-view-all {
  width: 100%;
  padding: 0.625rem;
  background: none;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  color: #4b5563;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-view-all:hover {
  background: #f3f4f6;
  border-color: #9ca3af;
}

/* Transiciones */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Responsive */
@media (max-width: 480px) {
  .notifications-dropdown {
    width: 90vw;
    right: -1rem;
  }
}
</style>
