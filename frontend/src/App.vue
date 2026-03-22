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

async function registrarTokenPushEnBackendConFetch(token: string): Promise<void> {
  await api.post('/notificaciones/token', {
      token,
      userAgent: navigator.userAgent
  })
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
