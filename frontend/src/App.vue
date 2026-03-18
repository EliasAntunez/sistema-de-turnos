<template>
  <ClienteNavbar v-if="showClientNavbar" />
  <UnifiedNavbar v-if="!showClientNavbar && !isUserView" />
  <router-view />
</template>

<script setup lang="ts">
import { computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificacionesStore } from '@/stores/notificaciones'
import { useToastStore } from '@/composables/useToast'
import { solicitarPermisoYObtenerToken, escucharNotificacionesForeground } from '@/firebase'
import UnifiedNavbar from '@/components/UnifiedNavbar.vue'
import ClienteNavbar from '@/components/ClienteNavbar.vue'

const authStore = useAuthStore()
const notificacionesStore = useNotificacionesStore()
const toastStore = useToastStore()
const route = useRoute()
let foregroundListenerActivo = false
let detenerForegroundListener: (() => void) | null = null

async function registrarTokenPushEnBackendConFetch(token: string): Promise<void> {
  const response = await fetch('http://localhost:8080/api/notificaciones/token', {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      token,
      userAgent: navigator.userAgent
    })
  })

  if (!response.ok) {
    throw new Error(`Error al registrar token push: HTTP ${response.status}`)
  }
}

async function inicializarFcmParaProfesional(): Promise<void> {
  try {
    const token = await solicitarPermisoYObtenerToken()
    if (!token) return

    await registrarTokenPushEnBackendConFetch(token)

    if (!foregroundListenerActivo) {
      detenerForegroundListener = await escucharNotificacionesForeground()
      foregroundListenerActivo = true
    }
  } catch (error) {
    console.error('Error al inicializar FCM para profesional', error)
  }
}

/**
 * Ciclo de vida global del WebSocket.
 * Se conecta cuando el usuario autenticado tiene rol PROFESIONAL y se
 * desconecta al cerrar sesión. Al ser global (App.vue), permanece activo
 * independientemente de la vista en que se encuentre el usuario multi-rol
 * (ej. DUENO+PROFESIONAL navegando entre /dueno y /profesional).
 */
watch(
  () => authStore.autenticado,
  async (estaAutenticado) => {
    try {
      if (estaAutenticado && authStore.hasRole('PROFESIONAL')) {
        await notificacionesStore.inicializar()
        await inicializarFcmParaProfesional()
      } else if (!estaAutenticado) {
        notificacionesStore.desconectar()
        if (detenerForegroundListener) {
          detenerForegroundListener()
          detenerForegroundListener = null
        }
        foregroundListenerActivo = false
      }
    } catch (error) {
      console.error('Error en watcher de autenticación', error)
    }
  },
  { immediate: true }
)

/**
 * Handler global de toasts para notificaciones WebSocket.
 * Al estar en App.vue, cubre todos los roles/vistas (incluyendo DUENO+PROFESIONAL
 * cuando está en /dueno y ProfesionalView está desmontado).
 *
 * Prevención de WS cruzados: omite el toast para CANCELACION_EMPRESA cuando
 * el usuario tiene rol DUENO, ya que DuenoView ya muestra su propio feedback
 * de éxito para cancelaciones que él mismo inició.
 */
const TIPOS_PROPIOS_DUENO = new Set(['CANCELACION_EMPRESA'])

function onNuevaNotificacionGlobal(event: Event) {
  const notificacion = (event as CustomEvent).detail
  if (authStore.hasRole('DUENO') && TIPOS_PROPIOS_DUENO.has(notificacion.tipo)) return
  toastStore.show(`${notificacion.titulo}: ${notificacion.mensaje}`, 4000, 'info')
}

onMounted(() => {
  window.addEventListener('nueva-notificacion', onNuevaNotificacionGlobal as EventListener)
})
onUnmounted(() => {
  window.removeEventListener('nueva-notificacion', onNuevaNotificacionGlobal as EventListener)
  if (detenerForegroundListener) {
    detenerForegroundListener()
    detenerForegroundListener = null
  }
  foregroundListenerActivo = false
})

const showClientNavbar = computed(() => !!route.meta?.clientView)
const isUserView = computed(() => ['Admin', 'Dueno', 'Profesional', 'Login'].includes(route.name as string))
</script>
