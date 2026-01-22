<template>
  <div class="min-h-screen bg-gradient-to-b from-gray-50 to-white flex flex-col items-center justify-center py-6 px-4 sm:px-6 lg:px-8">
    
    <!-- Card de login -->
    <div class="w-full max-w-md">
      <div class="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
        
        <!-- Header con gradiente -->
        <div class="bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-8 text-center">
          <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4 shadow-lg">
            <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-white">
            Bienvenido
          </h2>
          <p class="text-blue-100 text-sm mt-1">
            Ingresa a tu cuenta para continuar
          </p>
        </div>

        <!-- Formulario -->
        <div class="px-6 py-8 space-y-5">
          
          <!-- Campo Teléfono -->
          <div>
            <label for="telefono" class="block text-sm font-semibold text-gray-700 mb-2">
              Teléfono
            </label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
                </svg>
              </div>
              <input 
                id="telefono" 
                v-model="formData.telefono" 
                type="tel" 
                required
                class="block w-full pl-12 pr-4 py-3.5 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm placeholder-gray-400 transition-all hover:border-gray-300"
                placeholder="+54 9 11 1234-5678"
                @keyup.enter="login"
              />
            </div>
          </div>

          <!-- Campo Contraseña -->
          <div>
            <div class="flex items-center justify-between mb-2">
              <label for="contrasena" class="block text-sm font-semibold text-gray-700">
                Contraseña
              </label>
              <button
                type="button"
                @click="togglePasswordVisibility"
                class="text-xs font-medium text-blue-600 hover:text-blue-700 transition-colors"
              >
                {{ showPassword ? 'Ocultar' : 'Mostrar' }}
              </button>
            </div>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                </svg>
              </div>
              <input 
                id="contrasena" 
                v-model="formData.contrasena" 
                :type="showPassword ? 'text' : 'password'"
                required
                class="block w-full pl-12 pr-12 py-3.5 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm placeholder-gray-400 transition-all hover:border-gray-300"
                placeholder="••••••••"
                @keyup.enter="login"
              />
              <button
                type="button"
                @click="togglePasswordVisibility"
                class="absolute inset-y-0 right-0 pr-4 flex items-center hover:opacity-70 transition-opacity"
              >
                <svg v-if="showPassword" class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L6.59 6.59m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/>
                </svg>
                <svg v-else class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
              </button>
            </div>
            <div class="mt-2 text-right">
              <button
                type="button"
                @click="mostrarRecuperarContrasena"
                class="text-xs font-medium text-gray-500 hover:text-blue-600 transition-colors"
              >
                ¿Olvidaste tu contraseña?
              </button>
            </div>
          </div>

          <!-- Mensaje de error -->
          <div v-if="errorMessage" class="rounded-xl bg-red-50 border-2 border-red-100 p-4">
            <div class="flex gap-3">
              <svg class="h-5 w-5 text-red-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              <div>
                <h3 class="text-sm font-semibold text-red-800">
                  Error de autenticación
                </h3>
                <p class="text-xs text-red-700 mt-1">
                  {{ errorMessage }}
                </p>
              </div>
            </div>
          </div>

          <!-- Botón de login -->
          <button 
            type="button"
            @click="login"
            :disabled="loading || !formularioValido"
            :class="[
              'w-full flex justify-center items-center gap-2 py-4 px-4 rounded-xl text-sm font-semibold text-white focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all transform',
              loading || !formularioValido
                ? 'bg-gray-300 cursor-not-allowed'
                : 'bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 hover:shadow-lg active:scale-[0.98]'
            ]"
          >
            <svg v-if="loading" class="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
            </svg>
            <span>{{ loading ? 'Iniciando sesión...' : 'Iniciar Sesión' }}</span>
          </button>
        </div>

        <!-- Footer con enlaces -->
        <div class="px-6 pb-6 space-y-3 border-t border-gray-100 pt-6">
          <p class="text-center text-sm text-gray-600">
            ¿No tienes cuenta? 
            <router-link 
              :to="`/empresa/${empresaSlug}/registro-cliente`" 
              class="font-semibold text-blue-600 hover:text-blue-700 transition-colors"
            >
              Regístrate aquí
            </router-link>
          </p>
          <p class="text-center text-sm text-gray-600">
            O 
            <router-link 
              :to="`/empresa/${empresaSlug}`"
              class="font-semibold text-blue-600 hover:text-blue-700 hover:underline transition-colors"
            >
              reservar turno sin cuenta
            </router-link>
          </p>
        </div>
      </div>

      <!-- Texto de ayuda -->
      <p class="text-center text-xs text-gray-500 mt-6">
        Al iniciar sesión, aceptas nuestros términos y condiciones
      </p>
    </div>

    <!-- Footer -->
    <div class="mt-8 w-full max-w-md">
      <Footer 
        v-if="empresa"
        :direccion="empresa.direccion"
        :ciudad="empresa.ciudad"
        :telefono="empresa.telefono"
        :email="empresa.email"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import { useClienteStore } from '@/stores/cliente'
import publicoService, { type EmpresaPublica } from '@/services/publico'
import Footer from '@/components/Footer.vue'

const route = useRoute()
const router = useRouter()
const clienteStore = useClienteStore()

const empresaSlug = ref(route.params.empresaSlug as string)
const empresa = ref<EmpresaPublica | null>(null)

interface FormData {
  telefono: string
  contrasena: string
}

const formData = ref<FormData>({
  telefono: '',
  contrasena: ''
})

const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)

const formularioValido = computed(() => {
  return formData.value.telefono && formData.value.contrasena
})

function togglePasswordVisibility() {
  showPassword.value = !showPassword.value
}

function mostrarRecuperarContrasena() {
  // TODO: Implementar recuperación de contraseña
  errorMessage.value = 'Contacta al establecimiento para recuperar tu contraseña.'
}

async function login() {
  errorMessage.value = ''
  
  if (!formularioValido.value) {
    errorMessage.value = 'Por favor completa todos los campos'
    return
  }

  loading.value = true

  try {
    const response = await api.loginCliente(empresaSlug.value, formData.value)
    
    if (response.data.exito) {
      // Guardar datos del cliente en el store
      clienteStore.setCliente(response.data.datos)
      console.debug('[LoginClienteView] cliente seteado en store:', clienteStore.cliente)
      
      // Redirigir a mis turnos
      router.push(`/empresa/${empresaSlug.value}/mis-turnos`)
    } else {
      errorMessage.value = response.data.mensaje || 'Error al iniciar sesión'
    }
  } catch (error: any) {
    console.error('Error en login:', error)
    
    if (error.response?.data?.mensaje) {
      errorMessage.value = error.response.data.mensaje
    } else if (error.response?.status === 401) {
      errorMessage.value = 'Teléfono o contraseña incorrectos'
    } else if (error.response?.status === 404) {
      errorMessage.value = 'No existe una cuenta con este teléfono. Regístrate primero.'
    } else if (error.response?.status === 400) {
      errorMessage.value = 'Datos inválidos. Verifica el formato del teléfono.'
    } else if (error.response?.status === 500) {
      errorMessage.value = 'Error en el servidor. Intenta nuevamente más tarde.'
    } else {
      errorMessage.value = 'Error de conexión. Verifica tu internet e intenta nuevamente.'
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  // Si ya hay un cliente autenticado localmente, redirigir a Mis Turnos
  if (clienteStore.isAuthenticated) {
    router.push(`/empresa/${empresaSlug.value}/mis-turnos`)
    return
  }

  // Cargar datos de la empresa
  try {
    empresa.value = await publicoService.obtenerEmpresa(empresaSlug.value)
  } catch (error) {
    console.error('Error al cargar empresa:', error)
  }
})
</script>

<style scoped>
/* Transiciones suaves para inputs */
input {
  transition: all 0.2s ease;
}

/* Gradiente sutil para el fondo */
.from-gray-50 {
  --tw-gradient-from: #f9fafb;
}

.to-white {
  --tw-gradient-to: #ffffff;
}

/* Animación para el spinner */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>