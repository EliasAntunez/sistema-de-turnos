<template>
  <ClienteNavbar v-if="showClientNavbar" />
  <UnifiedNavbar v-if="!showClientNavbar && !isUserView" />
  <router-view />
</template>

<script setup lang="ts">
import { computed, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useNotificacionesStore } from '@/stores/notificaciones'
import { solicitarPermisoYObtenerToken, escucharNotificacionesForeground } from '@/firebase'
import api from '@/services/api'
import UnifiedNavbar from '@/components/UnifiedNavbar.vue'
import ClienteNavbar from '@/components/ClienteNavbar.vue'

const authStore = useAuthStore()
const notificacionesStore = useNotificacionesStore()
const route = useRoute()
let foregroundListenerActivo = false
let detenerForegroundListener: (() => void) | null = null
let isRegistering = false

const LAST_REGISTERED_FCM_TOKEN_KEY = 'last_registered_fcm_token'

function obtenerUltimoTokenRegistrado(): string | null {
  try {
    return sessionStorage.getItem(LAST_REGISTERED_FCM_TOKEN_KEY)
  } catch {
    return null
  }
}

function guardarUltimoTokenRegistrado(token: string): void {
  try {
    sessionStorage.setItem(LAST_REGISTERED_FCM_TOKEN_KEY, token)
  } catch {}
}

async function registrarTokenPushEnBackendConFetch(token: string): Promise<void> {
  if (!token) return

  const ultimoTokenRegistrado = obtenerUltimoTokenRegistrado()
  if (ultimoTokenRegistrado === token) {
    return
  }

  if (isRegistering) {
    return
  }

  isRegistering = true
  try {
    await api.post('/notificaciones/token', {
      token,
      userAgent: navigator.userAgent
    })
    guardarUltimoTokenRegistrado(token)
  } finally {
    isRegistering = false
  }
}

async function inicializarFcmParaProfesional(): Promise<void> {
  try {
    // 1. Pedimos el token directamente (si ya dio permiso, no pregunta; si no, salta el cartel)
    const token = await solicitarPermisoYObtenerToken()
    
    if (!token) {
      console.warn('[FCM] No se obtuvo token (Permiso denegado o error)')
      return
    }

    // 2. Registramos en el backend (el blindaje ya está en registrarTokenPushEnBackendConFetch)
    await registrarTokenPushEnBackendConFetch(token)

    // 3. Escuchamos notificaciones en primer plano
    if (!foregroundListenerActivo) {
      detenerForegroundListener = await escucharNotificacionesForeground()
      foregroundListenerActivo = true
    }
  } catch (error) {
    console.error('Error al inicializar FCM:', error)
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
  () => ({
    autenticado: authStore.autenticado,
    ruta: route.fullPath,
    roles: authStore.usuario?.roles ?? []
  }),
  async ({ autenticado, ruta }) => {
    try {
      const esVistaReservaCliente = ruta.includes('/reservar/')
      const esStaff = authStore.hasRole('PROFESIONAL') || authStore.hasRole('DUENO')

      if (!esVistaReservaCliente && autenticado && esStaff) {
        await notificacionesStore.inicializar()
        if (authStore.hasRole('PROFESIONAL')) {
          await inicializarFcmParaProfesional()
        }
      } else {
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

onUnmounted(() => {
  if (detenerForegroundListener) {
    detenerForegroundListener()
    detenerForegroundListener = null
  }
  foregroundListenerActivo = false
})

const showClientNavbar = computed(() => !!route.meta?.clientView)
const isUserView = computed(() => ['Admin', 'Dueno', 'Profesional', 'Login'].includes(route.name as string))
</script>
