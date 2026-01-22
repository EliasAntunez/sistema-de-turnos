<template>
  <header class="bg-white border-b border-gray-200 shadow-sm sticky top-0 z-50">
    <div class="max-w-7xl mx-auto px-3 sm:px-4 lg:px-8">
      <div class="flex items-center justify-between h-14 sm:h-16">
        <!-- Logo y Nombre de la Empresa -->
        <div class="flex items-center gap-2 sm:gap-3 flex-1 min-w-0">
          <!-- Logo / Volver al inicio -->
          <div class="flex items-center gap-2 p-1 sm:p-1.5">
            <div class="flex items-center justify-center w-7 h-7 sm:w-8 sm:h-8 rounded-lg bg-gradient-to-br from-blue-50 to-indigo-50">
              <svg class="w-4 h-4 sm:w-5 sm:h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 7v10a2 2 0 002 2h14a2 2 0 002-2V7M8 7V4a2 2 0 012-2h4a2 2 0 012 2v3"/>
              </svg>
            </div>
            <div class="hidden sm:block text-left">
              <div class="text-base font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent truncate max-w-[180px] lg:max-w-xs">
                {{ empresaNombreDisplay }}
              </div>
              <div v-if="empresaCiudad" class="text-xs text-gray-500 font-medium">
                {{ empresaCiudad }}
              </div>
            </div>
          </div>

          <!-- Nombre de la empresa en móvil -->
          <div class="sm:hidden ml-1">
            <div class="text-base font-bold text-blue-600 truncate max-w-[150px]">
              {{ empresaNombreDisplay }}
            </div>
          </div>
        </div>

        <!-- Acciones - SOLO para NO autenticados en vista pública -->
        <div class="flex items-center gap-2 sm:gap-3">
          <!-- Menú hamburguesa móvil (solo si no hay empresaSlug) -->
          <div v-if="!empresaSlug" class="sm:hidden relative">
            <button
              @click="toggleMobileMenu"
              class="p-2 rounded-lg hover:bg-gray-100 transition-colors"
              aria-label="Menú principal"
            >
              <svg class="w-6 h-6 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
              </svg>
            </button>
          </div>

          <!-- Botones de escritorio -->
          <div class="hidden sm:flex items-center gap-2">

            <!-- Si estamos en vista de empresa específica (empresaSlug existe) -->
            <template v-if="empresaSlug">
              <!-- Usuario NO autenticado: solo ver Ingresar/Registrarse -->
              <button
                @click="goLogin"
                class="px-3 sm:px-4 py-2 sm:py-2.5 bg-white border border-gray-300 text-gray-700 font-medium rounded-lg text-sm hover:bg-gray-50 hover:border-gray-400 transition-all duration-200 whitespace-nowrap"
              >
                <span class="flex items-center gap-1.5">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1"/>
                  </svg>
                  Ingresar
                </span>
              </button>
              <button
                @click="goRegister"
                class="px-3 sm:px-4 py-2 sm:py-2.5 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-medium rounded-lg text-sm hover:from-blue-700 hover:to-indigo-700 transition-all duration-200 shadow-sm hover:shadow whitespace-nowrap"
              >
                <span class="flex items-center gap-1.5">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
                  </svg>
                  Registrarse
                </span>
              </button>
            </template>
          </div>

          <!-- En móvil con empresaSlug: solo botón de acción principal -->
          <div v-if="empresaSlug" class="sm:hidden">
            <button
              @click="goRegister"
              class="px-3 py-2 bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-medium rounded-lg text-sm"
            >
              Reservar
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()

// Estado para el menú móvil
const mobileMenuOpen = ref(false)

// Cerrar menú móvil al hacer clic fuera
const closeMobileMenu = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.sm\\:hidden')) {
    mobileMenuOpen.value = false
  }
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

const empresaSlug = computed(() => (route.params.empresaSlug as string) || '')
const empresaNombre = ref<string | null>(null)
const empresaCiudad = ref<string | null>(null)

// Cargar datos básicos de empresa si existe el slug
onMounted(() => {
  if (empresaSlug.value) {
    // Aquí iría la llamada a la API para obtener empresa
    // Por ahora, usamos valores por defecto
    empresaNombre.value = empresaSlug.value.charAt(0).toUpperCase() + empresaSlug.value.slice(1)
    empresaCiudad.value = 'Local'
  }
})

const empresaNombreDisplay = computed(() => {
  if (empresaNombre.value) return empresaNombre.value
  return empresaSlug.value ? 'Turnero' : 'Turnero SaaS'
})

function goLogin() {
  const slug = empresaSlug.value || undefined
  if (slug) {
    router.push({ name: 'LoginCliente', params: { empresaSlug: slug } })
  } else {
    router.push({ path: '/login' })
  }
}

function goRegister() {
  const slug = empresaSlug.value || undefined
  if (slug) {
    // Si hay empresaSlug, ir directamente a registro para esa empresa
    router.push({ name: 'RegistroCliente', params: { empresaSlug: slug } })
  } else {
    // Si no hay empresa, ir a landing de registro
    router.push({ path: '/register' })
  }
}

function goToLogin() {
  router.push({ path: '/login' })
}
</script>

<style scoped>
.max-w-7xl { max-width: 80rem; }

/* Mejoras de hover más suaves */
button, a {
  transition: all 0.2s ease-in-out;
}

/* Overlay para fondo oscuro en móvil */
@media (max-width: 767px) {
  header {
    position: sticky;
    top: 0;
    z-index: 40;
  }
  
  .absolute {
    z-index: 50;
  }
}
</style>