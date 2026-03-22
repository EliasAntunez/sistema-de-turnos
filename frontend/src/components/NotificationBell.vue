<template>
  <div class="relative">
    <button
      @click="toggleDropdown"
      :class="[
        'relative inline-flex items-center justify-center rounded-full p-2 transition-colors',
        mostrarDropdown ? 'bg-white/15' : 'hover:bg-white/10'
      ]"
      title="Notificaciones"
    >
      <svg class="h-6 w-6 text-slate-200 transition-colors hover:text-white" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 0 0 5.454-1.31A8.967 8.967 0 0 1 18 9.75V9A6 6 0 0 0 6 9v.75a8.967 8.967 0 0 1-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 0 1-5.714 0m5.714 0a3 3 0 1 1-5.714 0" /></svg>
      <span
        v-if="store.contadorNoLeidas > 0"
        :class="[
          'absolute -right-0.5 -top-0.5 inline-flex h-4 w-4 items-center justify-center rounded-full bg-rose-500 text-[10px] font-semibold text-white',
          hayNotificacionesRecientes ? 'animate-pulse' : ''
        ]"
      >
        {{ store.contadorNoLeidas > MAX_BADGE_NUMBER ? '9+' : store.contadorNoLeidas }}
      </span>
    </button>

    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="translate-y-2 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-2 opacity-0"
    >
      <div v-if="mostrarDropdown" class="fixed left-4 right-4 top-16 z-[999] flex w-auto flex-col overflow-hidden rounded-xl border border-slate-200 bg-white shadow-xl sm:absolute sm:left-auto sm:right-0 sm:top-[calc(100%+0.5rem)] sm:w-80">
        <div class="flex items-center justify-between border-b border-slate-200 bg-slate-50 px-4 py-3">
          <h3>Notificaciones</h3>
          <div class="flex items-center gap-2">
            <button
              v-if="store.tieneNotificacionesNoLeidas"
              @click="marcarTodasLeidas"
              class="text-xs font-semibold text-teal-700 hover:text-teal-800 transition-colors"
              title="Marcar todas como leídas"
            >
              Marcar como leídas
            </button>
          </div>
        </div>

        <div v-if="store.cargando" class="flex flex-col items-center justify-center px-4 py-10 text-sm text-slate-500">
          <div class="mb-3 h-6 w-6 animate-spin rounded-full border-2 border-slate-200 border-t-teal-600"></div>
          <p>Cargando notificaciones...</p>
        </div>

        <div v-else-if="store.notificaciones && store.notificaciones.length > 0" class="max-h-[28rem] overflow-y-auto">
          <NotificationItem
            v-for="notificacion in notificacionesMostradas"
            :key="notificacion.id"
            :notificacion="notificacion"
            @click="handleNotificationClick(notificacion)"
          />
        </div>

        <div v-else class="flex flex-col items-center justify-center px-4 py-10 text-sm text-slate-500">
          <svg class="mb-2 h-8 w-8 text-slate-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 8.25l7.5 5.25a2.5 2.5 0 003 0L21 8.25M5.25 19.5h13.5A2.25 2.25 0 0021 17.25v-10.5A2.25 2.25 0 0018.75 4.5H5.25A2.25 2.25 0 003 6.75v10.5A2.25 2.25 0 005.25 19.5z" />
          </svg>
          <p>No hay notificaciones</p>
        </div>

        <div class="border-t border-slate-200 bg-slate-50 px-4 py-3">
          <button @click="verTodas" class="w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm font-medium text-slate-700 transition hover:bg-slate-100">
            Ver todas las notificaciones
          </button>
        </div>
      </div>
    </Transition>

    <div v-if="mostrarDropdown" class="fixed inset-0 z-[998]" @click="cerrarDropdown"></div>
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
let timerId: ReturnType<typeof setTimeout> | null = null

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
}

/**
 * Listener para nuevas notificaciones
 */
function onNuevaNotificacion() {
  hayNotificacionesRecientes.value = true

  if (timerId) {
    clearTimeout(timerId)
  }
  
  // Remover el efecto después de 5 segundos
  timerId = setTimeout(() => {
    hayNotificacionesRecientes.value = false
    timerId = null
  }, 5000)
}

// Lifecycle
onMounted(() => {
  // Escuchar eventos de nuevas notificaciones
  window.addEventListener('nueva-notificacion', onNuevaNotificacion as EventListener)
})

onUnmounted(() => {
  if (timerId) {
    clearTimeout(timerId)
    timerId = null
  }

  window.removeEventListener('nueva-notificacion', onNuevaNotificacion as EventListener)
})
</script>

<style scoped></style>
