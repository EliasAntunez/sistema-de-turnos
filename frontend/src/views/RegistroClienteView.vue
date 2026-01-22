<template>
  <div class="min-h-screen bg-gradient-to-b from-gray-50 to-white flex flex-col items-center justify-center py-6 px-4 sm:px-6 lg:px-8">

    <!-- Card de registro -->
    <div class="w-full max-w-md">
      <div class="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
        
        <!-- Header con gradiente (igual que login) -->
        <div class="bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-8 text-center">
          <div class="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4 shadow-lg">
            <svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/>
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-white">
            Registra tu cuenta
          </h2>
          <p class="text-blue-100 text-sm mt-1">
            Gestiona tus turnos y reservas fácilmente
          </p>
        </div>

        <!-- Formulario -->
        <form @submit.prevent="registrar" class="px-6 py-8 space-y-5">
          
          <!-- Campo Teléfono -->
          <div>
            <div class="flex items-center justify-between mb-2">
              <label for="telefono" class="block text-sm font-semibold text-gray-700">
                Teléfono
              </label>
              <span v-if="telefonoPrecargado" class="inline-flex items-center px-2 py-1 rounded-lg text-xs font-semibold bg-blue-100 text-blue-800 border border-blue-200">
                Precargado
              </span>
            </div>
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
                :readonly="telefonoPrecargado"
                :class="[
                  'block w-full pl-12 pr-4 py-3.5 border-2 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm transition-all',
                  telefonoPrecargado 
                    ? 'border-blue-200 bg-blue-50 text-gray-600' 
                    : 'border-gray-200 hover:border-gray-300 text-gray-900 placeholder-gray-400'
                ]"
                :placeholder="telefonoPrecargado ? 'Teléfono precargado' : '+54 9 11 1234-5678'"
              />
            </div>
            <p v-if="telefonoPrecargado" class="mt-2 text-xs text-gray-600">
              Este teléfono está vinculado a reservas anteriores
            </p>
            <p v-else class="mt-2 text-xs text-gray-500">
              Usa el mismo teléfono que usaste anteriormente (si aplica)
            </p>
          </div>

          <!-- Campo Email -->
          <div>
            <label for="email" class="block text-sm font-semibold text-gray-700 mb-2">
              Email
            </label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 4.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                </svg>
              </div>
              <input 
                id="email" 
                v-model="formData.email" 
                type="email" 
                required
                class="block w-full pl-12 pr-4 py-3.5 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm placeholder-gray-400 transition-all hover:border-gray-300"
                placeholder="tu@email.com"
              />
            </div>
          </div>

          <!-- Campo Contraseña -->
          <div>
            <label for="contrasena" class="block text-sm font-semibold text-gray-700 mb-2">
              Contraseña
            </label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                </svg>
              </div>
              <input 
                id="contrasena" 
                v-model="formData.contrasena" 
                type="password" 
                required
                class="block w-full pl-12 pr-10 py-3.5 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm placeholder-gray-400 transition-all hover:border-gray-300"
                placeholder="••••••••"
                @input="validarContrasena"
              />
              <div v-if="contrasenaValidada.mayuscula && contrasenaValidada.minuscula && contrasenaValidada.numero && contrasenaValidada.longitud" 
                  class="absolute inset-y-0 right-0 pr-4 flex items-center">
                <svg class="h-5 w-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
              </div>
            </div>
            
            <!-- Indicadores de fortaleza de contraseña -->
            <div class="mt-3 space-y-2">
              <div v-for="rule in passwordRules" :key="rule.key" class="flex items-center">
                <div :class="[
                  'w-2 h-2 rounded-full mr-3 transition-colors',
                  contrasenaValidada[rule.key] ? 'bg-green-500' : 'bg-gray-200'
                ]"></div>
                <span :class="[
                  'text-xs font-medium transition-colors',
                  contrasenaValidada[rule.key] ? 'text-gray-700' : 'text-gray-400'
                ]">
                  {{ rule.text }}
                </span>
              </div>
            </div>
          </div>

          <!-- Campo Confirmar Contraseña -->
          <div>
            <label for="confirmarContrasena" class="block text-sm font-semibold text-gray-700 mb-2">
              Confirmar Contraseña
            </label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/>
                </svg>
              </div>
              <input 
                id="confirmarContrasena" 
                v-model="formData.confirmarContrasena" 
                type="password" 
                required
                :class="[
                  'block w-full pl-12 pr-10 py-3.5 border-2 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm placeholder-gray-400 transition-all',
                  formData.contrasena && formData.confirmarContrasena && formData.contrasena === formData.confirmarContrasena
                    ? 'border-green-300 bg-green-50/30 hover:border-green-400'
                    : formData.confirmarContrasena
                    ? 'border-red-300 bg-red-50/30 hover:border-red-400'
                    : 'border-gray-200 hover:border-gray-300'
                ]"
                placeholder="••••••••"
              />
              <div v-if="formData.contrasena && formData.confirmarContrasena && formData.contrasena === formData.confirmarContrasena" 
                  class="absolute inset-y-0 right-0 pr-4 flex items-center">
                <svg class="h-5 w-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                </svg>
              </div>
            </div>
          </div>

          <!-- Mensajes de estado -->
          <div v-if="errorMessage" class="rounded-xl bg-red-50 border-2 border-red-100 p-4">
            <div class="flex gap-3">
              <svg class="h-5 w-5 text-red-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
              <div>
                <h3 class="text-sm font-semibold text-red-800">
                  Error de registro
                </h3>
                <p class="text-xs text-red-700 mt-1">
                  {{ errorMessage }}
                </p>
              </div>
            </div>
          </div>

          <div v-if="successMessage" class="rounded-xl bg-green-50 border-2 border-green-100 p-4">
            <div class="flex gap-3">
              <svg class="h-5 w-5 text-green-500 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
              </svg>
              <div>
                <h3 class="text-sm font-semibold text-green-800">
                  ¡Registro exitoso!
                </h3>
                <p class="text-xs text-green-700 mt-1">
                  {{ successMessage }}
                </p>
              </div>
            </div>
          </div>

          <!-- Botón de registro -->
          <button 
            type="submit" 
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
            <span>{{ loading ? 'Creando cuenta...' : 'Crear Cuenta' }}</span>
          </button>

          <!-- Enlace a login -->
          <div class="pt-6 border-t border-gray-100 space-y-3">
            <p class="text-center text-sm text-gray-600">
              ¿Ya tienes cuenta? 
              <router-link 
                :to="`/empresa/${empresaSlug}/login-cliente`" 
                class="font-semibold text-blue-600 hover:text-blue-700 transition-colors"
              >
                Inicia sesión aquí
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
        </form>
      </div>

      <!-- Texto de ayuda -->
      <p class="text-center text-xs text-gray-500 mt-6">
        Al crear una cuenta, aceptas nuestros términos y condiciones
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
const telefono = ref(route.query.telefono as string || '')
const telefonoPrecargado = ref(!!route.query.telefono)

interface FormData {
  telefono: string
  email: string
  contrasena: string
  confirmarContrasena: string
}

interface PasswordValidation {
  longitud: boolean
  mayuscula: boolean
  minuscula: boolean
  numero: boolean
}

interface PasswordRule {
  key: keyof PasswordValidation
  text: string
}

const formData = ref<FormData>({
  telefono: telefono.value,
  email: '',
  contrasena: '',
  confirmarContrasena: ''
})

const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const contrasenaValidada = ref<PasswordValidation>({
  longitud: false,
  mayuscula: false,
  minuscula: false,
  numero: false
})

const passwordRules: PasswordRule[] = [
  { key: 'longitud', text: 'Mínimo 8 caracteres' },
  { key: 'mayuscula', text: 'Al menos una mayúscula' },
  { key: 'minuscula', text: 'Al menos una minúscula' },
  { key: 'numero', text: 'Al menos un número' }
]

const formularioValido = computed(() => {
  return (
    formData.value.telefono &&
    formData.value.email &&
    formData.value.contrasena &&
    formData.value.confirmarContrasena &&
    formData.value.contrasena === formData.value.confirmarContrasena &&
    Object.values(contrasenaValidada.value).every(v => v)
  )
})

function validarContrasena() {
  const pass = formData.value.contrasena
  contrasenaValidada.value = {
    longitud: pass.length >= 8,
    mayuscula: /[A-Z]/.test(pass),
    minuscula: /[a-z]/.test(pass),
    numero: /\d/.test(pass)
  }
}

async function registrar() {
  errorMessage.value = ''
  successMessage.value = ''

  if (!formularioValido.value) {
    if (formData.value.contrasena !== formData.value.confirmarContrasena) {
      errorMessage.value = 'Las contraseñas no coinciden'
    } else {
      errorMessage.value = 'Por favor completa todos los campos correctamente'
    }
    return
  }

  loading.value = true

  try {
    const response = await api.registrarCliente(empresaSlug.value, formData.value)
    
    if (response.data.exito) {
      successMessage.value = '¡Cuenta creada exitosamente! Redirigiendo...'
      
      // Guardar datos del cliente en el store
      clienteStore.setCliente(response.data.datos)
      console.debug('[RegistroClienteView] cliente seteado en store:', clienteStore.cliente)
      
      // Redirigir a mis turnos después de 1.5 segundos
      setTimeout(() => {
        router.push(`/empresa/${empresaSlug.value}/mis-turnos`)
      }, 1500)
    } else {
      errorMessage.value = response.data.mensaje || 'Error al registrar'
    }
  } catch (error: any) {
    console.error('Error en registro:', error)
    
    if (error.response?.data?.errores) {
      const errores = error.response.data.errores
      const mensajes = Object.values(errores).flat() as string[]
      errorMessage.value = mensajes.join('. ')
    } else if (error.response?.data?.mensaje) {
      errorMessage.value = error.response.data.mensaje
    } else if (error.response?.status === 400) {
      errorMessage.value = 'Datos inválidos. Verifica la información ingresada.'
    } else if (error.response?.status === 409) {
      errorMessage.value = 'Ya existe una cuenta con este teléfono o email'
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

/* Estilo para el badge precargado */
.bg-blue-100 {
  background-color: #dbeafe;
}

/* Mejora en los indicadores de contraseña */
.bg-green-500 {
  background-color: #10b981;
}

/* Gradiente sutil para el fondo */
.from-gray-50 {
  --tw-gradient-from: #f9fafb;
}

.to-white {
  --tw-gradient-to: #ffffff;
}
</style>