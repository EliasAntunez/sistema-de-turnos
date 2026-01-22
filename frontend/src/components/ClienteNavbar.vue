<template>
  <nav class="bg-white border-b border-gray-200 shadow-sm">
    <div class="max-w-7xl mx-auto px-3 sm:px-4 lg:px-8">
      <div class="flex items-center justify-between h-14 sm:h-16">
        <!-- Logo y Nombre del Usuario (izquierda) -->
        <div class="flex items-center gap-2 sm:gap-4 flex-1 min-w-0">
          <!-- Nombre de la Empresa (siempre visible) -->
          <div class="flex items-center min-w-0">
            <div class="text-lg sm:text-xl font-bold bg-gradient-to-r from-indigo-600 to-blue-600 bg-clip-text text-transparent truncate">
              {{ empresaNombre || 'Turnero' }}
            </div>
          </div>

          <!-- Separador solo desktop -->
          <div v-if="isAuthenticated && clientePerteneceAEmpresa" class="hidden sm:block h-5 w-px bg-gray-300"></div>

          <!-- Saludo al Cliente (solo cuando pertenece) -->
          <div v-if="isAuthenticated && clientePerteneceAEmpresa" class="hidden sm:flex items-center min-w-0">
            <div class="flex items-center gap-2 min-w-0">
              <!-- Nombre del cliente -->
              <div class="flex flex-col leading-tight min-w-0">
                <span class="text-xs text-gray-500 font-medium truncate">Bienvenido</span>
                <span class="text-sm font-semibold text-gray-900 truncate max-w-[120px] sm:max-w-[150px]" :title="clienteFullName">
                  {{ clienteFullName || 'Usuario' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Botones de Acción (derecha) -->
        <div class="flex items-center gap-2 sm:gap-3">
          <!-- 1) Autenticado y cliente de la empresa actual -->
          <template v-if="isAuthenticated && clientePerteneceAEmpresa">
            <!-- Menú móvil (hamburguesa) para opciones del usuario -->
            <div class="md:hidden relative">
              <button
                @click="toggleMobileMenu"
                class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
                aria-label="Menú usuario"
              >
                <div class="w-6 h-6 rounded-full bg-gradient-to-br from-blue-100 to-indigo-100 flex items-center justify-center">
                  <span class="text-xs font-semibold text-blue-600">
                    {{ clienteNombre.charAt(0).toUpperCase() || 'U' }}
                  </span>
                </div>
              </button>
              
              <!-- Menú desplegable móvil -->
              <div v-if="mobileMenuOpen" class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50">
                <div class="px-4 py-3 border-b border-gray-100">
                  <div class="text-sm font-semibold text-gray-900 truncate">{{ clienteFullName || 'Usuario' }}</div>
                  <div class="text-xs text-gray-500">Bienvenido</div>
                </div>
                <button
                  @click="irAReservar"
                  class="flex items-center gap-2 w-full px-4 py-3 text-sm text-gray-700 hover:bg-gray-50"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                  </svg>
                  Reservar Turno
                </button>
                <button
                  @click="irAMisTurnos"
                  class="flex items-center gap-2 w-full px-4 py-3 text-sm text-gray-700 hover:bg-gray-50"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                  </svg>
                  Mis Turnos
                </button>
                <button 
                  @click="cerrarSesion"
                  class="flex items-center gap-2 w-full px-4 py-3 text-sm text-red-600 hover:bg-gray-50 border-t border-gray-100"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
                  </svg>
                  Salir
                </button>
              </div>
            </div>

            <!-- Botones de escritorio -->
            <div class="hidden md:flex items-center gap-2">
              <button
                @click="irAReservar"
                class="px-3 sm:px-4 py-2 sm:py-2.5 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-medium rounded-lg text-sm hover:from-blue-700 hover:to-indigo-700 transition-all duration-200 shadow-sm hover:shadow whitespace-nowrap"
              >
                <span class="hidden sm:flex items-center gap-1.5">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/>
                  </svg>
                  Reservar
                </span>
                <span class="sm:hidden">+</span>
              </button>
              
              <button
                @click="irAMisTurnos"
                class="px-3 sm:px-4 py-2 sm:py-2.5 bg-white border border-gray-300 text-gray-700 font-medium rounded-lg text-sm hover:bg-gray-50 hover:border-gray-400 transition-all duration-200 whitespace-nowrap"
              >
                <span class="hidden sm:flex items-center gap-1.5">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
                  </svg>
                  Turnos
                </span>
                <span class="sm:hidden">📅</span>
              </button>
              
              <button 
                @click="cerrarSesion"
                class="px-3 sm:px-4 py-2 sm:py-2.5 bg-white border border-gray-300 text-gray-700 font-medium rounded-lg text-sm hover:bg-gray-50 hover:border-gray-400 transition-all duration-200 whitespace-nowrap"
              >
                <span class="hidden sm:flex items-center gap-1.5">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
                  </svg>
                  Salir
                </span>
                <span class="sm:hidden">🚪</span>
              </button>
            </div>
          </template>

          <!-- 2) Autenticado pero cliente NO pertenece a la empresa actual -->
          <template v-else-if="isAuthenticated && !clientePerteneceAEmpresa">
            <div class="flex items-center gap-2 sm:gap-3">
              <div class="hidden sm:block px-3 py-1.5 bg-amber-50 border border-amber-200 rounded-lg">
                <span class="text-sm font-medium text-amber-700 flex items-center gap-1.5">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                  Otra empresa
                </span>
              </div>
              <div class="sm:hidden px-2 py-1 bg-amber-50 border border-amber-200 rounded-lg">
                <span class="text-xs font-medium text-amber-700">⚠️</span>
              </div>
              <button 
                @click="cerrarSesion"
                class="px-3 sm:px-4 py-2 sm:py-2.5 bg-white border border-gray-300 text-gray-700 font-medium rounded-lg text-sm hover:bg-gray-50 hover:border-gray-400 transition-all duration-200 whitespace-nowrap"
              >
                <span class="hidden sm:inline">Salir</span>
                <span class="sm:hidden">🚪</span>
              </button>
            </div>
          </template>

          <!-- 3) No autenticado -->
          <template v-else>
            <!-- Desktop: botones normales -->
            <div class="hidden md:flex items-center gap-3">
              <button
                @click="goLogin"
                class="px-4 py-2.5 bg-white border border-gray-300 text-gray-700 font-medium rounded-lg text-sm hover:bg-gray-50 hover:border-gray-400 transition-all duration-200"
              >
                Ingresar
              </button>
              <button
                @click="goRegister"
                class="px-4 py-2.5 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-medium rounded-lg text-sm hover:from-blue-700 hover:to-indigo-700 transition-all duration-200 shadow-sm hover:shadow"
              >
                Crear Cuenta
              </button>
            </div>

            <!-- Mobile: menú hamburguesa -->
            <div class="md:hidden relative">
              <button
                @click="toggleGuestMenu"
                class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
                aria-label="Menú"
              >
                <div class="w-6 h-6 rounded-full bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center">
                  <svg class="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                  </svg>
                </div>
              </button>
              
              <!-- Menú desplegable móvil -->
              <div v-if="guestMenuOpen" class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-50">
                <div class="px-4 py-3 border-b border-gray-100">
                  <div class="text-sm font-semibold text-gray-900">Bienvenido</div>
                  <div class="text-xs text-gray-500">Accede o regístrate</div>
                </div>
                <button
                  @click="goLogin"
                  class="flex items-center gap-2 w-full px-4 py-3 text-sm text-gray-700 hover:bg-gray-50"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/>
                  </svg>
                  Ingresar
                </button>
                <button
                  @click="goRegister"
                  class="flex items-center gap-2 w-full px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 border-t border-gray-100"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
                  </svg>
                  Crear Cuenta
                </button>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useClienteStore } from '@/stores/cliente'
import publicoService from '@/services/publico'

const route = useRoute()
const router = useRouter()
const clienteStore = useClienteStore()
const guestMenuOpen = ref(false)

// Estado para el menú móvil
const mobileMenuOpen = ref(false)

// Cerrar menú móvil al hacer clic fuera
const closeMobileMenu = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.md\\:hidden')) {
    mobileMenuOpen.value = false
  }
}

const toggleGuestMenu = () => {
  guestMenuOpen.value = !guestMenuOpen.value
}

// Toggle menú móvil
const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

// Cerrar menú móvil al cambiar de ruta
watch(() => route.path, () => {
  mobileMenuOpen.value = false
})

// Agregar event listener para cerrar menú al hacer clic fuera
onMounted(() => {
  document.addEventListener('click', closeMobileMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeMobileMenu)
})

const isAuthenticated = computed(() => clienteStore.isAuthenticated)
const clienteNombre = computed(() => clienteStore.cliente?.nombre || '')
const clienteFullName = computed(() => {
  const nombre = clienteStore.cliente?.nombre || ''
  const telefono = clienteStore.cliente?.telefono || ''
  const fallback = clienteStore.cliente?.email || ''
  return nombre || telefono || fallback || ''
})

const empresaSlug = computed(() => (route.params.empresaSlug as string) || '')

const empresaIdActual = ref<number | null>(null)
const empresaCargada = ref(false)
const empresaNombre = ref<string | null>(null)
const empresaCiudad = ref<string | null>(null)

async function cargarEmpresa() {
  empresaCargada.value = false
  empresaIdActual.value = null
  if (!empresaSlug.value) return
  try {
    const e = await publicoService.obtenerEmpresa(empresaSlug.value)
    empresaIdActual.value = e.id
    empresaNombre.value = e.nombre
    empresaCiudad.value = e.ciudad
  } catch (err) {
    empresaIdActual.value = null
  } finally {
    empresaCargada.value = true
  }
}

onMounted(() => {
  void cargarEmpresa()
})

watch(() => route.params.empresaSlug, () => {
  void cargarEmpresa()
})

function irAReservar() {
  mobileMenuOpen.value = false
  if (!empresaSlug.value) return
  router.push({ name: 'Reservar', params: { empresaSlug: empresaSlug.value } })
}

function irAMisTurnos() {
  mobileMenuOpen.value = false
  if (!empresaSlug.value) return
  router.push({ name: 'MisTurnos', params: { empresaSlug: empresaSlug.value } })
}

function goLogin() {
  guestMenuOpen.value = false
  mobileMenuOpen.value = false
  const slug = empresaSlug.value || undefined
  if (slug) router.push({ name: 'LoginCliente', params: { empresaSlug: slug } })
  else router.push({ path: '/login' })
}

function goRegister() {
  guestMenuOpen.value = false
  mobileMenuOpen.value = false
  const slug = empresaSlug.value || undefined
  if (slug) router.push({ name: 'RegistroCliente', params: { empresaSlug: slug } })
  else router.push({ path: '/login' })
}

const clientePerteneceAEmpresa = computed(() => {
  if (!isAuthenticated.value) return false
  if (!empresaCargada.value) return false
  const clienteEmpresaId = clienteStore.cliente?.empresaId
  if (clienteEmpresaId === undefined || clienteEmpresaId === null) return false
  return clienteEmpresaId === empresaIdActual.value
})

async function cerrarSesion() {
  mobileMenuOpen.value = false
  try {
    const api = await import('@/services/api')
    await api.default.logout()
  } catch (e) {
    // ignore
  }
  clienteStore.logout()
  const authMod = await import('@/stores/auth')
  authMod.useAuthStore().logout()
  window.location.reload()
}
</script>

<style scoped>
.max-w-7xl { max-width: 80rem; }

/* Mejoras de hover más suaves */
button {
  transition: all 0.2s ease-in-out;
}

/* Overlay para fondo oscuro en móvil (opcional) */
@media (max-width: 767px) {
  nav {
    position: relative;
    z-index: 40;
  }
  
  .absolute {
    z-index: 50;
  }
}

/* Mejor contraste en móvil */
@media (max-width: 640px) {
  .text-transparent {
    color: #4f46e5 !important;
    background-image: none !important;
  }
}
</style>